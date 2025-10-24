package com.saunhardy.omnieconomy;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(OmniEconomy.MODID)
public class OmniEconomy {
    public static final String MODID = "omnieconomy";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> BILL_1    = ITEMS.register("bill_1",    () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_5    = ITEMS.register("bill_5",    () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_10   = ITEMS.register("bill_10",   () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_20   = ITEMS.register("bill_20",   () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_50   = ITEMS.register("bill_50",   () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_100  = ITEMS.register("bill_100",  () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_500  = ITEMS.register("bill_500",  () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> BILL_1000 = ITEMS.register("bill_1000", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CIRCUIT_BOARD = ITEMS.register("circuit_board", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> KEYPAD = ITEMS.register("keypad", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> MOD_ICON =
            ITEMS.register("mod_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("mod_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.omnieconomy"))
                    .icon(() -> MOD_ICON.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(BILL_1.get());
                        out.accept(BILL_5.get());
                        out.accept(BILL_10.get());
                        out.accept(BILL_20.get());
                        out.accept(BILL_50.get());
                        out.accept(BILL_100.get());
                        out.accept(BILL_500.get());
                        out.accept(BILL_1000.get());
                        out.accept(CIRCUIT_BOARD.get());
                        out.accept(KEYPAD.get());
                    })
                    .build()
            );

    public OmniEconomy(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) { LOGGER.info("OmniEconomy: common setup"); }
}
