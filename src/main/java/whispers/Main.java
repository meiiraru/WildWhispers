package whispers;

import cinnamon.Cinnamon;
import cinnamon.Client;
import whispers.screens.MainMenu;

public class Main {

    public static void main(String... args) {
        Cinnamon.TITLE = "Wild Whispers";
        Client.mainScreen = MainMenu::new;
        new Cinnamon().run();
    }
}
