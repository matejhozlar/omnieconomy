package com.saunhardy.omnieconomy;

import com.mojang.logging.LogUtils;
import com.saunhardy.omnieconomy.block.ATMBlock;
import com.saunhardy.omnieconomy.client.ClientOnlyHooks;
import com.saunhardy.omnieconomy.command.LotteryCommands;
import com.saunhardy.omnieconomy.command.MoneyCommands;
import com.saunhardy.omnieconomy.datagen.DataGenerators;
import com.saunhardy.omnieconomy.enchantment.ModEnchantmentEffects;
import com.saunhardy.omnieconomy.events.StockTickerIntegration;
import com.saunhardy.omnieconomy.item.BankCardItem;
import com.saunhardy.omnieconomy.lottery.LotteryManager;
import com.saunhardy.omnieconomy.menu.ATMMenu;
import com.saunhardy.omnieconomy.mobdrops.MobDrops;
import com.saunhardy.omnieconomy.network.ATMNetworking;
import com.saunhardy.omnieconomy.reward.PlaytimeRewardsManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(OmniEconomy.MODID)
public class OmniEconomy {
    public static final String MODID = "omnieconomy";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LotteryManager lotteryManager = new LotteryManager(
            Config.ENABLE_LOTTERY,
            Config.LOTTERY_MIN_BET,
            Config.LOTTERY_COOLDOWN_MINUTES,
            Config.LOTTERY_DURATION_SECONDS
    );

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MODID);

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
    public static final DeferredItem<BankCardItem> BANK_CARD = ITEMS.register("bank_card", () -> new BankCardItem(new Item.Properties().stacksTo(1)));

    public static final DeferredBlock<ATMBlock> ATM_BLOCK = BLOCKS.register("atm", () ->
            new ATMBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ATMMenu>> ATM_MENU =
            MENUS.register("atm", () -> new MenuType<>(ATMMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredItem<BlockItem> ATM_ITEM =
            ITEMS.register("atm", () -> new BlockItem(ATM_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> MOD_ICON =
            ITEMS.register("mod_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("mod_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.omnieconomy"))
                    .icon(() -> MOD_ICON.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(ATM_ITEM.get());
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
                        out.accept(BANK_CARD.get());
                    })
                    .build()
            );

    public OmniEconomy(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(DataGenerators::gatherData);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModEnchantmentEffects.register(modEventBus);

        modEventBus.addListener(ATMNetworking::register);

        NeoForge.EVENT_BUS.register(PlaytimeRewardsManager.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientOnlyHooks::registerScreens);
        }

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            NeoForge.EVENT_BUS.register(MobDrops.class);
            NeoForge.EVENT_BUS.register(MoneyCommands.class);
            NeoForge.EVENT_BUS.addListener(this::onRegisterCommandsLottery);
            NeoForge.EVENT_BUS.addListener(this::onServerTickPost);
        }

        if (ModList.get().isLoaded("create")) {
            NeoForge.EVENT_BUS.register(StockTickerIntegration.class);
            LOGGER.info("Create mod detected - Stock Ticker integration enabled");
        }


        NeoForge.EVENT_BUS.addListener(BackupHooks::onLevelSave);

    }

    private void onRegisterCommandsLottery(RegisterCommandsEvent event) {
        if (!Config.ENABLE_LOTTERY.get()) return;
        LotteryCommands.register(event.getDispatcher(), lotteryManager);
    }

    private void onServerTickPost(ServerTickEvent.Post e) {
        if (!Config.ENABLE_LOTTERY.get()) return;
        lotteryManager.tick(e.getServer());
    }

    private void commonSetup(final FMLCommonSetupEvent event) { LOGGER.info("OmniEconomy: common setup"); }
}
