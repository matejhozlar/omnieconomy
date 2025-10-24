package com.saunhardy.omnieconomy.datagen;

import com.saunhardy.omnieconomy.OmniEconomy;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OmniEconomy.CIRCUIT_BOARD.get())
                .pattern("PG")
                .pattern("RI")
                .define('P', Items.COPPER_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OmniEconomy.KEYPAD.get())
                .pattern("BBB")
                .pattern(" I ")
                .define('B', Items.STONE_BUTTON)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_button", has(Items.STONE_BUTTON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, OmniEconomy.ATM_ITEM.get())
                .pattern("ICI")
                .pattern("CBC")
                .pattern("IKI")
                .define('I', Items.IRON_BLOCK)
                .define('C', OmniEconomy.CIRCUIT_BOARD.get())
                .define('B', Items.CHEST)
                .define('K', OmniEconomy.KEYPAD.get())
                .unlockedBy("has_circuit_board", has(OmniEconomy.CIRCUIT_BOARD.get()))
                .save(output);
    }
}
