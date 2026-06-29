package whispers;

import cinnamon.Cinnamon;
import cinnamon.Client;
import cinnamon.events.EventType;
import cinnamon.gui.GUISkin;
import cinnamon.world.Hud;
import whispers.screens.MainMenu;

public class Main {

    public static void main(String... args) {
        Cinnamon.TITLE = "Wild Whispers";
        Client.mainScreen = MainMenu::new;
        Client.getInstance().events.registerEvent(EventType.CLIENT_INIT, event -> GUISkin.setCurrentSkin(Hud.SKIN));
        new Cinnamon().run();
    }
}
