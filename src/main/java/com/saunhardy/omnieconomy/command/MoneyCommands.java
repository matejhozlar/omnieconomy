package com.saunhardy.omnieconomy.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.OmniEconomy;
import com.saunhardy.omnieconomy.core.Economy;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class MoneyCommands {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static long getCooldownMs() { return Config.COMMAND_COOLDOWN_MS.get(); }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COOLDOWNS.remove(event.getEntity().getUUID());
    }

    private static boolean isOnCooldown(ServerPlayer player) {
        long now = System.currentTimeMillis();
        long last = COOLDOWNS.getOrDefault(player.getUUID(), 0L);
        if (now - last < getCooldownMs()) {
            long sec = (getCooldownMs() - (now - last)) / 1000;
            player.sendSystemMessage(msg("[ERROR]", "Please wait " + sec + "s before using this command again.", ChatFormatting.RED));
            return true;
        }
        COOLDOWNS.put(player.getUUID(), now);
        return false;
    }

    private static Component msg(String tarOrEmoji, String text, ChatFormatting color) {
        return Component.literal(tarOrEmoji + " " + text).withStyle(color);
    }
    private static String fmt(int amount) { return NumberFormat.getInstance().format(amount); }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        if (Config.ENABLE_MONEY_COMMAND.get()) {
            d.register(Commands.literal("money")
                    .executes( ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (isOnCooldown(player)) return 0;
                        int bal = Economy.getBalance(ctx.getSource().getServer(), player.getUUID());
                        player.sendSystemMessage(msg("💰", "Balance: " + Config.CURRENCY_SYMBOL.get() + fmt(bal), ChatFormatting.GREEN));
                        return 1;
                    })
            );
        }

        if (Config.ENABLE_PAY_COMMAND.get()) {
            d.register(Commands.literal("pay")
                    .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        ServerPlayer from = ctx.getSource().getPlayerOrException();
                        if (isOnCooldown(from)) return 0;

                        ServerPlayer to = EntityArgument.getPlayer(ctx, "target");
                        int amount = IntegerArgumentType.getInteger(ctx, "amount");

                        Economy.PayResult res = Economy.pay(ctx.getSource().getServer(), from.getUUID(), to.getUUID(), amount);
                        switch (res) {
                            case SUCCESS -> {
                                String f = fmt(amount);
                                from.sendSystemMessage(msg("✅", "Sent " + Config.CURRENCY_SYMBOL.get() + f + " to " + to.getName().getString(), ChatFormatting.GREEN));
                                to.sendSystemMessage(msg("\uD83D\uDCB8", "You recieved " + Config.CURRENCY_SYMBOL.get() + f + " from " + from.getName().getString(), ChatFormatting.GOLD));
                            }
                            case INSUFFICIENT_FUNDS -> from.sendSystemMessage(msg("[ERROR]", "Insufficient funds.", ChatFormatting.RED));
                            case RECEIVER_CLAMPED -> from.sendSystemMessage(msg("[ERROR]", "Reciever would exceed max balance.", ChatFormatting.RED));
                            case SELF_PAYMENT -> from.sendSystemMessage(msg("[ERROR]", "Can't pay yourself.", ChatFormatting.RED));
                            default -> from.sendSystemMessage(msg("[ERROR]", "Invalid amount.", ChatFormatting.RED));
                        }
                        return 1;
                    })
                    ))
            );
        }

            if (Config.ENABLE_DEPOSIT_COMMAND.get()) {
                d.register(Commands.literal("deposit")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            if (isOnCooldown(player)) return 0;
                            handleDepositAll(ctx.getSource().getServer(), player);
                            return 1;
                        })
                );
            }

            if (Config.ENABLE_WITHDRAW_COMMAND.get()) {
                d.register(Commands.literal("withdraw")
                        .then(Commands.argument("input", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            if (isOnCooldown(player)) return 0;
                            String input = StringArgumentType.getString(ctx, "input").trim();

                            if (input.matches("^\\d+ \\d+$")) {
                                String[] parts = input.split(" ");
                                int denom = Integer.parseInt(parts[0]);
                                int count = Integer.parseInt(parts[1]);
                                return withdrawFixed(ctx.getSource().getServer(), player, denom, count);
                            }
                            if (input.contains(":")) {
                                return withdrawBundle(ctx.getSource().getServer(), player, input);
                            }
                            if (input.matches("^\\d+$")) {
                                int total = Integer.parseInt(input);
                                return withdrawOptimized(ctx.getSource().getServer(), player, total);
                            }
                            player.sendSystemMessage(msg("[ERROR]", "Invalid command format.", ChatFormatting.RED));
                            return 0;
                        })
                        )
                );
            }
        if (Config.ENABLE_BALTOP_COMMAND.get()) {
            d.register(Commands.literal("baltop")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (isOnCooldown(player)) return 0;

                        var top = Economy.getBaltop(ctx.getSource().getServer(), Config.BALTOP_SIZE.get());
                        if (top.isEmpty()) {
                            player.sendSystemMessage(msg("[ERROR]", "No data found.", ChatFormatting.RED));
                            return 1;
                        }

                        player.sendSystemMessage(msg("\uD83C\uDFC6", "Top " + Config.BALTOP_SIZE.get() + " Richest Players:", ChatFormatting.GREEN));
                        int rank = 1;
                        for (var e : top) {
                            UUID id = e.getKey();
                            int bal = e.getValue();
                            String name = Optional.ofNullable(ctx.getSource().getServer().getPlayerList().getPlayer(id))
                                    .map(pl -> pl.getName().getString())
                                    .orElse(id.toString().substring(0, 8));
                            player.sendSystemMessage(Component.literal(" " + rank + ". " + name + ": " + Config.CURRENCY_SYMBOL.get() + fmt(bal)));
                            rank++;
                        }
                        return 1;
                    })
            );
        }

        if (Config.ENABLE_DAILY_REWARDS.get()) {
            d.register(Commands.literal("daily")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (isOnCooldown(player)) return 0;

                        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
                        if (!Economy.canClaimDaily(ctx.getSource().getServer(), player.getUUID(), today)) {
                            player.sendSystemMessage(msg("[ERROR]", "You already claimed your reward today.", ChatFormatting.RED));
                            return 1;
                        }
                        boolean ok = Economy.claimDaily(ctx.getSource().getServer(), player.getUUID(), today, Config.DAILY_REWARD_AMOUNT.get());
                        if (ok) player.sendSystemMessage(msg("✅", "Reward of " + Config.CURRENCY_SYMBOL.get() + Config.DAILY_REWARD_AMOUNT.get() + " claimed!", ChatFormatting.GREEN));
                        else player.sendSystemMessage(msg("[ERROR]", "Unable to claim reward.", ChatFormatting.RED));
                        return 1;
                    })
            );
        }
    }

    public static void handleDepositAll(MinecraftServer server, ServerPlayer player) {
        final Map<Item, Integer> billValues = Map.of(
                OmniEconomy.BILL_1.get(), 1,
                OmniEconomy.BILL_5.get(), 5,
                OmniEconomy.BILL_10.get(), 10,
                OmniEconomy.BILL_20.get(), 20,
                OmniEconomy.BILL_50.get(), 50,
                OmniEconomy.BILL_100.get(), 100,
                OmniEconomy.BILL_500.get(), 500,
                OmniEconomy.BILL_1000.get(), 1000
        );

        final Map<Integer, List<Integer>> slotsByDenom = new HashMap<>();
        int total = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                Integer v = billValues.get(stack.getItem());
                if (v != null) {
                    total += v * stack.getCount();
                    slotsByDenom.computeIfAbsent(v, k -> new ArrayList<>()).add(i);
                }
            }
        }

        if (total == 0) {
            player.sendSystemMessage(msg("[ERROR]", "No bills to deposit.", ChatFormatting.RED));
            return;
        }

        for (List<Integer> slots : slotsByDenom.values()) {
            for (int slot : slots) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
        player.inventoryMenu.broadcastChanges();

        Economy.deposit(server, player.getUUID(), total);
        player.sendSystemMessage(msg("✅", "Deposited " + Config.CURRENCY_SYMBOL.get() + fmt(total) +" into your account!", ChatFormatting.GREEN));
    }

    private static int withdrawFixed(MinecraftServer server, ServerPlayer player, int denomination, int count) {
        Item billItem = getBillItem(denomination);
        if (billItem == null) {
            player.sendSystemMessage(msg("[ERROR]", "Invalid denomination.", ChatFormatting.RED));
            return 0;
        }
        if (inventoryLacksSpace(player, billItem, count)) {
            player.sendSystemMessage(msg("[ERROR]", "Not enough inventory space for" + fmt(count) + "bills.", ChatFormatting.RED));
            return 0;
        }

        int total = denomination * count;
        boolean ok = Economy.withdraw(server, player.getUUID(), total);
        if (!ok) {
            player.sendSystemMessage(msg("[ERROR]", "Insufficient funds.", ChatFormatting.RED));
            return 0;
        }

        ItemStack stack = new ItemStack(billItem, count);
        player.getInventory().placeItemBackInInventory(stack);
        player.inventoryMenu.broadcastChanges();
        player.sendSystemMessage(msg("✅", "Successfully withdew " + Config.CURRENCY_SYMBOL.get() + fmt(total), ChatFormatting.GREEN));
        return 1;
    }

    private static int withdrawBundle(MinecraftServer server, ServerPlayer player, String input) {
        Map<Integer, Integer> bundle = new LinkedHashMap<>();
        int total = 0;

        for (String part : input.split(" ")) {
            String[] pair = part.split(":");
            if (pair.length != 2) {
                player.sendSystemMessage(msg("[ERROR]", "Invalid format: " + part, ChatFormatting.RED));
                return 0;
            }
            int denom, count;
            try {
                denom = Integer.parseInt(pair[0]);
                count = Integer.parseInt(pair[1]);
            } catch (NumberFormatException ex) {
                player.sendSystemMessage(msg("[ERROR]", "Invalid number in: " + part, ChatFormatting.RED));
                return 0;
            }

            Item item = getBillItem(denom);
            if (item == null) {
                player.sendSystemMessage(msg("[ERROR]", "Unsupported denomination: " + Config.CURRENCY_SYMBOL.get() + denom, ChatFormatting.RED));
                return 0;
            }
            if (inventoryLacksSpace(player, item, count)) {
                player.sendSystemMessage(msg("[ERROR]", "Not enough inventory space for " + Config.CURRENCY_SYMBOL.get() + denom + " x " + count, ChatFormatting.RED));
                return 0;
            }
            bundle.put(denom, count);
            total += denom * count;
        }

        boolean ok = Economy.withdraw(server, player.getUUID(), total);
        if (!ok) {
            player.sendSystemMessage(msg("[ERROR]", "Insufficient funds.", ChatFormatting.RED));
            return 0;
        }

        for (var e : bundle.entrySet()) {
            ItemStack stack = new ItemStack(Objects.requireNonNull(getBillItem(e.getKey())), e.getValue());
            player.getInventory().placeItemBackInInventory(stack);
        }
        player.inventoryMenu.broadcastChanges();

        player.sendSystemMessage(msg("✅", "Successfully withdrew " + Config.CURRENCY_SYMBOL.get() + fmt(total), ChatFormatting.GREEN));

        return 1;
    }

    private static int withdrawOptimized(net.minecraft.server.MinecraftServer server, ServerPlayer player, int totalAmount) {
        int[] denoms = {1000, 500, 100, 50, 20, 10, 5, 1};
        Map<Integer, Integer> result = new LinkedHashMap<>();
        int remaining = totalAmount;

        for (int d : denoms) {
            int count = remaining / d;
            if (count > 0) {
                result.put(d, count);
                remaining -= d * count;
            }
        }

        if (remaining > 0) {
            player.sendSystemMessage(msg("[ERROR]", "Cannot make exact change.", ChatFormatting.RED));
            return 0;
        }

        for (var e : result.entrySet()) {
            Item billItem = getBillItem(e.getKey());
            if (billItem == null) {
                player.sendSystemMessage(msg("[ERROR]", "Invalid denomination: $" + e.getKey(), ChatFormatting.RED));
                return 0;
            }
            if (inventoryLacksSpace(player, billItem, e.getValue())) {
                player.sendSystemMessage(msg("[ERROR]", "Not enough inventory space for " + fmt(e.getValue()) + " bills.", ChatFormatting.RED));
                return 0;
            }
        }

        boolean ok = Economy.withdraw(server, player.getUUID(), totalAmount);
        if (!ok) {
            player.sendSystemMessage(msg("[ERROR]", "Insufficient funds.", ChatFormatting.RED));
            return 0;
        }

        for (var e : result.entrySet()) {
            ItemStack stack = new ItemStack(Objects.requireNonNull(getBillItem(e.getKey())), e.getValue());
            player.getInventory().placeItemBackInInventory(stack);
        }
        player.inventoryMenu.broadcastChanges();

        player.sendSystemMessage(msg("✅", "Successfully withdrew " + Config.CURRENCY_SYMBOL.get() + fmt(totalAmount), ChatFormatting.GREEN));
        return 1;
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

    private static boolean inventoryLacksSpace(ServerPlayer player, Item item, int totalCount) {
        int maxStack = new ItemStack(item).getMaxStackSize();
        int remaining = totalCount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot.isEmpty())             remaining -= maxStack;
            else if (slot.getItem() == item) remaining -= (maxStack - slot.getCount());
            if (remaining <= 0) return false;
        }
        return true;
    }
}
