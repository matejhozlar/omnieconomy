package com.saunhardy.omnieconomy.datagen;

import com.saunhardy.omnieconomy.OmniEconomy;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends BlockTagsProvider {
    public ModBlockTags(PackOutput output,
                        CompletableFuture<HolderLookup.Provider> lookup,
                        ExistingFileHelper efh) {
        super(output, lookup, OmniEconomy.MODID, efh);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(OmniEconomy.ATM_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(OmniEconomy.ATM_BLOCK.get());
    }

    @Override
    public @NotNull String getName() { return "OmniEconomy Block Tags"; }
}
