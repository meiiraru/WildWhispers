package whispers;

import cinnamon.Cinnamon;
import cinnamon.Client;
import cinnamon.events.Events;
import cinnamon.gui.GUISkin;
import cinnamon.world.gui.Hud;
import whispers.screens.MainMenu;

public class Main {

    public static void main(String... args) {
        Cinnamon.TITLE = "Wild Whispers";
        Client.mainScreen = MainMenu::new;
        Events.CLIENT_INIT.register("set_skin", () -> GUISkin.setCurrentSkin(Hud.SKIN));
        new Cinnamon().run();
    }
}
