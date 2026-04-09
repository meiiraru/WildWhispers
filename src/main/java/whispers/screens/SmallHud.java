package whispers.screens;

import cinnamon.Client;
import cinnamon.gui.widgets.types.ProgressBar;
import cinnamon.model.GeometryHelper;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.utils.Colors;
import cinnamon.utils.Resource;
import cinnamon.world.Hud;
import whispers.entities.ThePlayer;

public class SmallHud extends Hud {

    public static final Resource icons = new Resource("whispers", "textures/icons.png");

    protected ProgressBar tiredness, hunger;

    @Override
    public void init() {
        health = new ProgressBar(0, 0, 60, 8, 1f);
        health.setColor(Colors.RED);
        health.setStyle(HUD_STYLE);

        tiredness = new ProgressBar(0, 20, 60, 8, 1f);
        tiredness.setColor(Colors.BLUE);
        tiredness.setStyle(HUD_STYLE);

        hunger = new ProgressBar(0, 40, 60, 8, 1f);
        hunger.setColor(Colors.ORANGE);
        hunger.setStyle(HUD_STYLE);
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        ThePlayer player = (ThePlayer) Client.getInstance().world.player;
        int width = Client.getInstance().window.scaledWidth;
        int height = Client.getInstance().window.scaledHeight;

        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, height - (4 + 8), 8, 8, 16f, 0f, 8f, 8f, 24, 8),
                icons
        );

        float hunger = player.getHunger() / 100f;
        this.hunger.setProgress(hunger);
        this.hunger.setPos(16, height - 4 - this.hunger.getHeight());
        this.hunger.render(matrices, 0, 0, delta);

        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, height - (4 + 8) * 2, 8, 8, 0f, 0f, 8f, 8f, 24, 8),
                icons
        );

        float tired = player.getTired() / 100f;
        tiredness.setProgress(tired);
        tiredness.setPos(16, this.hunger.getY() - 4 - tiredness.getHeight());
        tiredness.render(matrices, 0, 0, delta);

        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, height - (4 + 8) * 3, 8, 8, 8f, 0f, 8f, 8f, 24, 8),
                icons
        );

        float hp = player.getHealthProgress();
        health.setProgress(hp);
        health.setPos(16,  tiredness.getY() - 4 - health.getHeight());
        health.render(matrices, 0, 0, delta);

        //draw vignette
        drawVignette(matrices, player, delta);
    }
}
