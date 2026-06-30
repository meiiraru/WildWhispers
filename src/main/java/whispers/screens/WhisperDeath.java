package whispers.screens;

import cinnamon.Client;
import cinnamon.gui.Screen;
import cinnamon.gui.screens.MainMenu;
import cinnamon.gui.widgets.ContainerGrid;
import cinnamon.gui.widgets.types.Button;
import cinnamon.gui.widgets.types.ConfirmPopup;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Style;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.utils.UIHelper;
import whispers.world.TestWorld;

public class WhisperDeath extends Screen {

    public WhisperDeath() {
        ((TestWorld) Client.getInstance().world).lives--;
    }

    @Override
    public void init() {
        super.init();

        ContainerGrid grid = new ContainerGrid(0, 0, 8, 2);

        Button respawn = new MainMenu.MainButton(Text.translated("whispers.respawn"), button -> {
            this.close();
            client.world.respawn(false);
        });
        boolean out = ((TestWorld) client.world).lives <= 0;
        respawn.setActive(!out);
        if (out) respawn.setTooltip(Text.translated("whispers.respawn.tooltip"));
        grid.addWidget(respawn);

        ConfirmPopup popup = new ConfirmPopup.YesNo(Text.translated("whispers.leave"), b -> {
            if (b) client.disconnect();
        });

        Button menu = new MainMenu.MainButton(Text.translated("whispers.main_menu"), button -> {
            UIHelper.setPopup(0, 0, popup);
            popup.open();
        });
        grid.addWidget(menu);

        grid.setPos((width - grid.getWidth()) / 2, (int) ((height - grid.getHeight()) * 0.66f));
        this.addWidget(grid);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        Text text;
        boolean gameOver = ((TestWorld) client.world).lives <= 0;
        if (gameOver) {
            text = Text.translated("whispers.game_over_1").append("\n\n").append(Text.translated("whispers.game_over_2", client.world.getDay() + 1));
        } else {
            text = Text.translated("whispers.ded");
        }

        text.withStyle(Style.EMPTY.outlined(true).color(0xFF880000))
                .render(VertexConsumer.MAIN, matrices, width / 2f, height / 2f - 60, Alignment.TOP_CENTER);
    }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta, int color1, int color2, float size) {
        renderSolidBackground(0x88 << 24);
    }
}
