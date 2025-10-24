package com.saunhardy.omnieconomy.network;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.OmniEconomy;
import com.saunhardy.omnieconomy.client.ATMScreen;
import com.saunhardy.omnieconomy.core.Economy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.*;

public final class ATMNetworking {
    private ATMNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(OmniEconomy.MODID).versioned("1");

        reg.playToServer(ATMDepositPayload.TYPE, ATMDepositPayload.STREAM_CODEC, ATMNetworking::handleDeposit);
        reg.playToServer(ATMWithdrawPayload.TYPE, ATMWithdrawPayload.STREAM_CODEC, ATMNetworking::handleWithdraw);
        reg.playToServer(ATMQueryBalancePayload.TYPE, ATMQueryBalancePayload.STREAM_CODEC, ATMNetworking::handleQueryBalance);

        reg.playToClient(ATMResultPayload.TYPE, ATMResultPayload.STREAM_CODEC, ATMNetworking::handleResultClient);
        reg.playToClient(ATMBalancePayload.TYPE, ATMBalancePayload.STREAM_CODEC, ATMNetworking::handleBalanceClient);

    }

    private static void handleBalanceClient(final ATMBalancePayload pkt, final IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        ctx.enqueueWork(() -> {
            if (mc.screen instanceof ATMScreen scr) {
                scr.updateBalance(pkt.balance());
            }
        });
    }

    private static void handleResultClient(final ATMResultPayload pkt, final IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        ctx.enqueueWork(() -> {
            if (mc.screen instanceof ATMScreen scr) {
                int color = switch (pkt.kind()) {
                    case 1 -> 0x2ECC71;
                    case 2 -> 0xE74C3C;
                    default -> 0xFFFFFF;
                };
                scr.showStatus(pkt.message(), color);
            } else if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal(pkt.message()), false);
            }
        });
    }

    private static void handleQueryBalance(final ATMQueryBalancePayload pkt, final IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            int bal = Economy.getBalance(player.server, player.getUUID());
            PacketDistributor.sendToPlayer(player, new ATMBalancePayload(Math.max(0, bal)));
        });
    }

    private static void handleDeposit(final ATMDepositPayload pkt, final IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            Map<Object, Integer> values = Map.of(
                    OmniEconomy.BILL_1.get(), 1,
                    OmniEconomy.BILL_5.get(), 5,
                    OmniEconomy.BILL_10.get(), 10,
                    OmniEconomy.BILL_20.get(), 20,
                    OmniEconomy.BILL_50.get(), 50,
                    OmniEconomy.BILL_100.get(), 100,
                    OmniEconomy.BILL_500.get(), 500,
                    OmniEconomy.BILL_1000.get(), 1000
            );

            Map<Integer, List<Integer>> slotsByDenom = new HashMap<>();
            int total = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack st = player.getInventory().getItem(i);
                if (!st.isEmpty() && values.containsKey(st.getItem())) {
                    int val = values.get(st.getItem());
                    total += val * st.getCount();
                    slotsByDenom.computeIfAbsent(val, k -> new ArrayList<>()).add(i);
                }
            }

            if (total <= 0) {
                sendResult(player, 2, "No bills to deposit.");
                return;
            }

            for (List<Integer> slots : slotsByDenom.values()) {
                for (int slot : slots) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
            player.inventoryMenu.broadcastChanges();

            Economy.deposit(player.server, player.getUUID(), total);

            sendResult(player, 1, "Deposited " + Config.CURRENCY_SYMBOL.get() + total);
            int bal = Economy.getBalance(player.server, player.getUUID());
            PacketDistributor.sendToPlayer(player, new ATMBalancePayload(bal));
        });
    }

    private static void handleWithdraw(final ATMWithdrawPayload pkt, final IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            final java.util.function.BiConsumer<Integer, Integer> give = (denom, count) -> {
                var item = switch (denom) {
                    case 1 -> OmniEconomy.BILL_1.get();
                    case 5 -> OmniEconomy.BILL_5.get();
                    case 10 -> OmniEconomy.BILL_10.get();
                    case 20 -> OmniEconomy.BILL_20.get();
                    case 50 -> OmniEconomy.BILL_50.get();
                    case 100 -> OmniEconomy.BILL_100.get();
                    case 500 -> OmniEconomy.BILL_500.get();
                    case 1000 -> OmniEconomy.BILL_1000.get();
                    default -> null;
                };
                if (item == null || count <= 0) return;
                ItemStack stack = new ItemStack(item, count);
                player.getInventory().placeItemBackInInventory(stack);
            };

            if (pkt.mode() == 0) {
                int denom = pkt.a();
                int count = pkt.b();
                long totalL = (long) denom * (long) count;
                if (totalL <= 0 || totalL > Integer.MAX_VALUE) {
                    sendResult(player, 2, "Invalid withdrawal amount.");
                    return;
                }
                int total = (int) totalL;

                boolean ok = Economy.withdraw(player.server, player.getUUID(), total);
                if (!ok) {
                    sendResult(player, 2, "Insufficient funds.");
                    return;
                }

                give.accept(denom, count);
                sendResult(player, 1, "Withdrew " + Config.CURRENCY_SYMBOL.get() + total);
                int bal = Economy.getBalance(player.server, player.getUUID());
                PacketDistributor.sendToPlayer(player, new ATMBalancePayload(bal));
            } else {
                int total = pkt.a();
                if (total <= 0) {
                    sendResult(player, 2, "Invalid amount.");
                    return;
                }

                int[] denoms = {1000, 500, 100, 50, 20, 10, 5, 1};
                Map<Integer, Integer> bundle = new LinkedHashMap<>();
                int remaining = total;
                for (int d : denoms) {
                    int cnt = remaining / d;
                    if (cnt > 0) {
                        bundle.put(d, cnt);
                        remaining -= d * cnt;
                    }
                }
                if (remaining != 0) {
                    sendResult(player, 2, "Cannot make exact change.");
                    return;
                }

                boolean ok = Economy.withdraw(player.server, player.getUUID(), total);
                if (!ok) {
                    sendResult(player, 2, "Insufficient funds.");
                    return;
                }

                for (var e : bundle.entrySet()) {
                    give.accept(e.getKey(), e.getValue());
                }
                sendResult(player, 1, "Withdrawal complete (" + Config.CURRENCY_SYMBOL.get() + total + ")");
                int bal = Economy.getBalance(player.server, player.getUUID());
                PacketDistributor.sendToPlayer(player, new ATMBalancePayload(bal));
            }
        });
    }

    private static void sendResult(ServerPlayer player, int kind, String msg) {
        PacketDistributor.sendToPlayer(player, new ATMResultPayload(kind, msg));
    }
}
