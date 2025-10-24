package com.saunhardy.omnieconomy.mobdrops;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.OmniEconomy;
import com.saunhardy.omnieconomy.enchantment.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MobDrops {
    private MobDrops() {}

    private static final Map<UUID, Integer> droppedToday = new ConcurrentHashMap<>();
    private static final Set<UUID> warnedToday = ConcurrentHashMap.newKeySet();
    private static volatile long lastResetEpochDay = Long.MIN_VALUE;
    private static final Set<EntityType<?>> BASE_MOBS = Set.of(
            EntityType.ZOMBIE, EntityType.CREEPER, EntityType.SPIDER, EntityType.SKELETON,
            EntityType.WITHER_SKELETON, EntityType.BLAZE
    );


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        warnedToday.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        warnedToday.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (!Config.ENABLE_MOB_DROPS.get()) return;

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player instanceof FakePlayer) return;
        if (player.isSpectator()) return;

        maybeResetDaily(Objects.requireNonNull(player.getServer()).getTickCount());

        final LivingEntity dead = event.getEntity();
        final EntityType<?> type = dead.getType();
        if (!BASE_MOBS.contains(type)) return;

        double oneDollarChance = 0.0;
        if (type == EntityType.ZOMBIE) {
            oneDollarChance = Config.DROP_ZOMBIE_1.get();
        } else if (type == EntityType.CREEPER) {
            oneDollarChance = Config.DROP_CREEPER_1.get();
        } else if (type == EntityType.SPIDER) {
            oneDollarChance = Config.DROP_SPIDER_1.get();
        } else if (type == EntityType.SKELETON) {
            oneDollarChance = Config.DROP_SKELETON_1.get();
        } else if (type == EntityType.WITHER_SKELETON) {
            oneDollarChance = Config.DROP_WITHER_SKELETON_1.get();
        } else if (type == EntityType.BLAZE) {
            oneDollarChance = Config.DROP_BLAZE_1.get();
        }

        int greedLevel = 0;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            var reg = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            var greedHolder = reg.getHolderOrThrow(ModEnchantments.CAPITALIST_GREED);
            greedLevel = mainHand.getEnchantmentLevel(greedHolder);
        }
        if (greedLevel > 0) {
            switch (Math.min(greedLevel, 3)) {
                case 1 -> oneDollarChance += Config.CAPITALIST_GREED_1.get();
                case 2 -> oneDollarChance += Config.CAPITALIST_GREED_2.get();
                case 3 -> oneDollarChance += Config.CAPITALIST_GREED_3.get();
            }
        }

        boolean fiveHit = false;
        if (Config.RARE_FIVE_DOLLAR_CHANCE.get() > 0.0) {
            double p5 = Config.RARE_FIVE_DOLLAR_CHANCE.get();
            if (ThreadLocalRandom.current().nextDouble() < p5) fiveHit = true;
        }

        boolean oneHit = false;
        if (oneDollarChance > 0.0) {
            double p1 = oneDollarChance / 100.0;
            if (ThreadLocalRandom.current().nextDouble() < p1) oneHit = true;
        }

        if (!oneHit && !fiveHit) return;

        final UUID id = player.getUUID();

        if (!Config.ENABLE_MOB_DROPS_LIMIT.get()) {
            if (fiveHit) dropBill(dead, OmniEconomy.BILL_5.get());
            if (oneHit)  dropBill(dead, OmniEconomy.BILL_1.get());
            return;
        }

        int cap = Math.max(0, Config.MOB_DROPS_LIMIT.get());
        int soFar = droppedToday.getOrDefault(id, 0);
        if (soFar >= cap) {
            maybeWarnLimit(player, cap);
            return;
        }

        int valueToAward = (fiveHit ? 5 : 0) + (oneHit ? 1 : 0);

        int allowed = Math.min(valueToAward, cap - soFar);
        if (allowed <= 0) {
            maybeWarnLimit(player, cap);
            return;
        }

        int remaining = allowed;
        boolean drop5 = false;
        boolean drop1 = false;

        if (fiveHit && remaining >= 5) {
            drop5 = true;
            remaining -= 5;
        }
        if (oneHit && remaining >= 1) {
            drop1 = true;
            remaining -= 1;
        }

        if (drop5) dropBill(dead, OmniEconomy.BILL_5.get());
        if (drop1) dropBill(dead, OmniEconomy.BILL_1.get());

        int newTotal = soFar + (allowed - remaining);
        droppedToday.put(id, newTotal);

        if (Config.WARN_ON_MOB_DROPS_LIMIT.get() && newTotal >= cap) {
            maybeWarnLimit(player, cap);
        }
    }


    private static void dropBill(LivingEntity dead, Item bill) {
        if (bill != null) {
            dead.spawnAtLocation(new ItemStack(bill, 1));
        }
    }

    private static void maybeWarnLimit(ServerPlayer player, int cap) {
        if (!Config.WARN_ON_MOB_DROPS_LIMIT.get()) return;
        if (warnedToday.add(player.getUUID())) {
            player.sendSystemMessage(Component
                    .literal("⚠ You've reached today's mob drop limit (" + cap + ").")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void maybeResetDaily(int tickCount) {
        if ((tickCount & 0x13) != 0) return;

        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        if (today == lastResetEpochDay) return;

        lastResetEpochDay = today;
        droppedToday.clear();
        warnedToday.clear();
    }
}