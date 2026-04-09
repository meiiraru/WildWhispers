package whispers.screens;

import cinnamon.gui.Screen;
import cinnamon.gui.widgets.types.Button;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Style;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;

public class WhisperDeath extends Screen {

    @Override
    public void init() {
        super.init();

        Button menu = new Button(width / 2 - 90, height / 2 - 20, 180, 20, Text.translated("gui.death_screen.main_menu"), button -> client.disconnect());
        this.addWidget(menu);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        Text.of("You died...").withStyle(Style.EMPTY.color(0xFF880000))
                .render(VertexConsumer.MAIN, matrices, width / 2f, height / 2f - 60, Alignment.TOP_CENTER);
    }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta, int color1, int color2, float size) {
        renderSolidBackground(0x88 << 24);
    }
}
