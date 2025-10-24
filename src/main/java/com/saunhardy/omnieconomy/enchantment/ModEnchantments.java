package com.saunhardy.omnieconomy.enchantment;

import com.saunhardy.omnieconomy.OmniEconomy;
import com.saunhardy.omnieconomy.enchantment.custom.CapitalistGreedEnchantmentEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> CAPITALIST_GREED = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(OmniEconomy.MODID, "capitalist_greed"));

    public  static void bootstrap(BootstrapContext<Enchantment> context) {
        var items = context.lookup(Registries.ITEM);

        Enchantment capitalistGreed = Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                3,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2,
                EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new CapitalistGreedEnchantmentEffect())
                .build(CAPITALIST_GREED.location());

        CAPITALIST_GREED_ENCHANTMENT = capitalistGreed;

        context.register(CAPITALIST_GREED, capitalistGreed);
    }

    public static Enchantment CAPITALIST_GREED_ENCHANTMENT;
}
