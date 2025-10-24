package com.saunhardy.omnieconomy.client;

import com.saunhardy.omnieconomy.OmniEconomy;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientOnlyHooks {
    private ClientOnlyHooks() {}

    public static void registerScreens(RegisterMenuScreensEvent e) {
        e.register(OmniEconomy.ATM_MENU.get(), ATMScreen::new);
    }
}
