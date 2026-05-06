package whispers.screens;

import cinnamon.gui.Screen;
import cinnamon.gui.screens.SettingsScreen;
import cinnamon.gui.widgets.ContainerGrid;
import cinnamon.gui.widgets.types.Button;
import cinnamon.model.GeometryHelper;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.render.texture.Texture;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import whispers.world.TestWorld;

import static cinnamon.gui.screens.MainMenu.GUI_STYLE;

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
         grid.setPos((width - grid.getWidth()) / 2, y + (height - grid.getHeight() - y) / 2);
         grid.setStyle(GUI_STYLE);
         this.addWidget(grid);
     }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta, int color1, int color2, float size) {
        super.renderBackground(matrices, delta, color1, color2, size);

        //559 x 235
        float w = 559 * 0.3f;
        float h = 235 * 0.3f;
        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, (width - w) / 2f, h / 2f, w, h),
                LOGO, Texture.TextureParams.SMOOTH_SAMPLING
        );
    }
}
