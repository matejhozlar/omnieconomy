package com.saunhardy.omnieconomy.util;

import com.saunhardy.omnieconomy.OmniEconomy;
import com.saunhardy.omnieconomy.core.Economy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WithdrawalHelper {
    public enum WithdrawalResult {
        SUCCESS,
        FAILED_API,
        FAILED_CONNECTION,
        FAILED_INVENTORY,
        FAILED_DEV_MODE
    }

    public static class WithdrawalResponse {
        public final WithdrawalResult result;
        public final boolean success;

        private WithdrawalResponse(WithdrawalResult result) {
            this.result = result;
            this.success = result == WithdrawalResult.SUCCESS || result == WithdrawalResult.FAILED_DEV_MODE;
        }

        public static WithdrawalResponse success() {
            return new WithdrawalResponse(WithdrawalResult.SUCCESS);
        }

        public static WithdrawalResponse failed(WithdrawalResult result) {
            return new WithdrawalResponse(result);
        }

        public static WithdrawalResponse devMode() {
            return new WithdrawalResponse(WithdrawalResult.FAILED_DEV_MODE);
        }
    }

    public static WithdrawalResponse withdrawBills(ServerPlayer player, Item billItem, int count, int denomination) {
        if (player == null || player.getServer() == null) return WithdrawalResponse.failed(WithdrawalResult.FAILED_CONNECTION);
        if (billItem == null || count <= 0 || denomination <= 0) return WithdrawalResponse.failed(WithdrawalResult.FAILED_API);

        Item expected = getBillItem(denomination);
        if (expected == null || expected != billItem) {
            return WithdrawalResponse.failed(WithdrawalResult.FAILED_API);
        }

        if (inventoryLacksSpace(player, billItem, count)) {
            return WithdrawalResponse.failed(WithdrawalResult.FAILED_INVENTORY);
        }

        int total = denomination * count;

        boolean ok = Economy.withdraw(player.getServer(), player.getUUID(), total);
        if (!ok) {
            return WithdrawalResponse.failed(WithdrawalResult.FAILED_API);
        }

        ItemStack stack = new ItemStack(billItem, count);
        player.getInventory().placeItemBackInInventory(stack);
        player.inventoryMenu.broadcastChanges();

        return WithdrawalResponse.success();
    }

    public static WithdrawalResponse withdrawBillsWithSpaceCheck(ServerPlayer player, Item billItem, int count, int denomination) {
        int slotsNeeded = calculateSlotsNeeded(count, new ItemStack(billItem).getMaxStackSize());
        if (!hasInventorySpace(player, slotsNeeded)) {
            return WithdrawalResponse.failed(WithdrawalResult.FAILED_INVENTORY);
        }
        return withdrawBills(player, billItem, count, denomination);
    }

    public static int calculateSlotsNeeded(int count, int maxStackSize) {
        if (maxStackSize <= 0) maxStackSize = 64;
        return (count + maxStackSize - 1) / maxStackSize;
    }

    public static boolean hasInventorySpace(ServerPlayer player, int slotsNeeded) {
        if (slotsNeeded <= 0) return true;
        int free = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) free++;
        }
        return free >= slotsNeeded;
    }

    private static boolean inventoryLacksSpace(ServerPlayer player, Item item, int totalCount) {
        int maxStack = new ItemStack(item).getMaxStackSize();
        int remaining = totalCount;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && slot.getItem() == item) {
                int canAdd = Math.max(0, maxStack - slot.getCount());
                if (canAdd > 0) {
                    int used = Math.min(canAdd, remaining);
                    remaining -= used;
                    if (remaining == 0) return false;
                }
            }
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot.isEmpty()) {
                int used = Math.min(maxStack, remaining);
                remaining -= used;
                if (remaining <= 0) return false;
            }
        }

        return remaining > 0;
    }

    private static Item getBillItem(int denom) {
        return switch (denom) {
            case 1    -> OmniEconomy.BILL_1.get();
            case 5    -> OmniEconomy.BILL_5.get();
            case 10   -> OmniEconomy.BILL_10.get();
            case 20   -> OmniEconomy.BILL_20.get();
            case 50   -> OmniEconomy.BILL_50.get();
            case 100  -> OmniEconomy.BILL_100.get();
            case 500  -> OmniEconomy.BILL_500.get();
            case 1000 -> OmniEconomy.BILL_1000.get();
            default   -> null;
        };
    }
}