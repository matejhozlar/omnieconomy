package com.saunhardy.omnieconomy.datagen;

import com.saunhardy.omnieconomy.OmniEconomy;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ModBlockLoot extends BlockLootSubProvider {

    public ModBlockLoot(HolderLookup.Provider lookup) {
        super(Set.of(), FeatureFlags.VANILLA_SET, lookup);
    }

    @Override
    protected void generate() {
        this.dropSelf(OmniEconomy.ATM_BLOCK.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return List.of(OmniEconomy.ATM_BLOCK.get());
    }
}
