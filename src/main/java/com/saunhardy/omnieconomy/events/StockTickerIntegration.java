package com.saunhardy.omnieconomy.events;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.BlockPos;

import static com.saunhardy.omnieconomy.OmniEconomy.*;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import java.util.Map;
import java.util.HashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;

import com.saunhardy.omnieconomy.util.WithdrawalHelper;

import java.lang.reflect.Method;


public class StockTickerIntegration {

    private static boolean getBills(Player player, ItemStack shoppingListItem) {
        var shoppingList = ShoppingListItem.getList(shoppingListItem);
        if (shoppingList == null) {
            return false;
        }

        try {
            Method bakeEntriesMethod = shoppingList.getClass().getMethod("bakeEntries",
                    net.minecraft.world.level.LevelAccessor.class, net.minecraft.core.BlockPos.class);
            Object bakedEntries = bakeEntriesMethod.invoke(shoppingList, player.level(), null);

            if (bakedEntries == null) {
                return false;
            }

            Method getSecondMethod = bakedEntries.getClass().getMethod("getSecond");
            Object paymentSummary = getSecondMethod.invoke(bakedEntries);

            Method getStacksByCountMethod = paymentSummary.getClass().getMethod("getStacksByCount");
            @SuppressWarnings("unchecked")
            var paymentStacks = (java.util.List<Object>) getStacksByCountMethod.invoke(paymentSummary);

            Map<Item, Integer> billValues = new HashMap<>();
            billValues.put(BILL_1.get(), 1);
            billValues.put(BILL_5.get(), 5);
            billValues.put(BILL_10.get(), 10);
            billValues.put(BILL_20.get(), 20);
            billValues.put(BILL_50.get(), 50);
            billValues.put(BILL_100.get(), 100);
            billValues.put(BILL_500.get(), 500);
            billValues.put(BILL_1000.get(), 1000);

            int totalSlotsNeeded = 0;
            Map<Item, Integer> billsToDispense = new HashMap<>();

            for (Object bigItemStackObj : paymentStacks) {
                var stackField = bigItemStackObj.getClass().getField("stack");
                var countField = bigItemStackObj.getClass().getField("count");

                ItemStack paymentItem = (ItemStack) stackField.get(bigItemStackObj);
                int requiredAmount = countField.getInt(bigItemStackObj);

                if (billValues.containsKey(paymentItem.getItem())) {
                    billsToDispense.put(paymentItem.getItem(), requiredAmount);

                    int slotsForThisBill = (int) Math.ceil((double) requiredAmount / 64);
                    totalSlotsNeeded += slotsForThisBill;
                }
            }

            if (totalSlotsNeeded > 0 && player instanceof ServerPlayer serverPlayer) {
                if (!WithdrawalHelper.hasInventorySpace(serverPlayer, totalSlotsNeeded)) {
                    player.sendSystemMessage(Component.translatable("message.omnieconomy.insufficient_inventory_space").withStyle(ChatFormatting.RED));
                    return false;
                }
                boolean allWithdrawalsSucceeded = true;

                for (Map.Entry<Item, Integer> entry : billsToDispense.entrySet()) {
                    Item billItem = entry.getKey();
                    int count = entry.getValue();
                    int denomination = billValues.get(billItem);

                    WithdrawalHelper.WithdrawalResponse response = WithdrawalHelper.withdrawBills(
                            serverPlayer, billItem, count, denomination);

                    if (!response.success) {
                        allWithdrawalsSucceeded = false;
                        player.sendSystemMessage(Component.translatable("message.omnieconomy.withdrawal_failed").withStyle(ChatFormatting.RED));
                        break;
                    }
                    player.sendSystemMessage(Component.translatable("message.omnieconomy.withdrawal_success", count, "$" + denomination).withStyle(ChatFormatting.GREEN));
                }

                return allWithdrawalsSucceeded;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }


    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockPos stockTickerPos = StockTickerInteractionHandler.getStockTickerPosition(target);
        if (stockTickerPos != null) {
            if ((heldItem.getItem() instanceof ShoppingListItem) && (player.getOffhandItem().is(BANK_CARD.get()))) {
                getBills(player, heldItem);
            }
        }
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onRightClickBlock(RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        BlockPos pos = event.getPos();
        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (event.getLevel().getBlockState(pos).getBlock() instanceof BlazeBurnerBlock) {
            if ((heldItem.getItem() instanceof ShoppingListItem) && (player.getOffhandItem().is(BANK_CARD.get()))) {
                getBills(player, heldItem);
            }
        }
    }
}