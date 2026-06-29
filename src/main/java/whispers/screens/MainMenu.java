package whispers.screens;

import cinnamon.gui.Screen;
import cinnamon.gui.screens.SettingsScreen;
import cinnamon.gui.widgets.ContainerGrid;
import cinnamon.gui.widgets.types.Button;
import cinnamon.math.Rotation;
import cinnamon.model.GeometryHelper;
import cinnamon.model.Vertex;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.render.texture.Texture;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import org.joml.Math;
import whispers.world.TestWorld;

public class MainMenu extends Screen {

    public static final Resource LOGO = new Resource("whispers", "textures/logo.png");

     @Override
     public void init() {
         super.init();

         //buttons
         ContainerGrid grid = new ContainerGrid(0, 0, 4);

         //open world
         Button worldButton = new cinnamon.gui.screens.MainMenu.MainButton(Text.translated("gui.play"), button -> new TestWorld().init());
         grid.addWidget(worldButton);

         //settings
         Button settings = new cinnamon.gui.screens.MainMenu.MainButton(Text.translated("gui.main_menu.settings"), button -> client.setScreen(new SettingsScreen(this)));
         settings.setActive(false);
         grid.addWidget(settings);

         //exit
         Button exitButton = new cinnamon.gui.screens.MainMenu.MainButton(Text.translated("gui.exit"), button -> client.window.exit());
         exitButton.setTooltip(Text.translated("gui.main_menu.exit.tooltip"));
         grid.addWidget(exitButton);

         //add grid to screen
         int y = (int) (height * 0.15f);
         grid.setPos((int) (width * 0.25f - grid.getWidth() / 2f), y + (height - grid.getHeight() - y) / 2);
         this.addWidget(grid);
     }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta, int color1, int color2, float size) {
        //super.renderBackground(matrices, delta, color1, color2, size);
        matrices.pushMatrix();
        matrices.translate(width / 2f, height / 2f, 0);

        float scale = Math.sin((client.ticks + delta) * 0.005f) * 0.5f + 0.5f + 1.4f;
        float angle = Math.sin((client.ticks + delta) * 0.005f) * 7f;

        matrices.scale(scale);
        matrices.rotate(Rotation.Z.rotationDeg(angle));

        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, -width/2f, -height/2f, width, height),
                new Resource("whispers", "textures/main_menu_bg.png"), Texture.TextureParams.SMOOTH_SAMPLING
        );
        matrices.popMatrix();

        Vertex[] vertices = GeometryHelper.rectangle(matrices, -width/2f + width * 0.25f, 0, width * 0.25f, height, 0x00000000);
        vertices[1].color(0x7F000000);
        vertices[2].color(0x7F000000);
        VertexConsumer.MAIN.consume(vertices);

        vertices = GeometryHelper.rectangle(matrices, width * 0.25f, 0, width/2f, height, 0x00000000);
        vertices[0].color(0x7F000000);
        vertices[3].color(0x7F000000);
        VertexConsumer.MAIN.consume(vertices);

        //559 x 235
        float w = 559 * 0.3f;
        float h = 235 * 0.3f;
        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, (width * 0.25f - w / 2f), h / 2f, w, h),
                LOGO, Texture.TextureParams.SMOOTH_SAMPLING
        );
    }
}
