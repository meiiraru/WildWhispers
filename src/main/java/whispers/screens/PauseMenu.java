package whispers.screens;

import cinnamon.gui.screens.world.PauseScreen;
import cinnamon.gui.widgets.ContainerGrid;
import cinnamon.gui.widgets.types.Button;
import cinnamon.model.GeometryHelper;
import cinnamon.model.Vertex;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Text;
import cinnamon.utils.Resource;

public class PauseMenu extends PauseScreen {

    @Override
    public void init() {
//        super.init();

        ContainerGrid grid = new ContainerGrid(0, 0, 8, 2);

        Button resume = new Button(0, 0, 120, 20, Text.translated("gui.pause_screen.resume"), button -> close());
        Button menu = new Button(0, 0, 120, 20, Text.translated("gui.pause_screen.main_menu"), button -> client.disconnect());

        grid.addWidget(resume);
        grid.addWidget(menu);

        grid.setPos((width - grid.getWidth()) / 2, (int) ((height - grid.getHeight()) * 0.66f));

        this.addWidget(grid);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //render controls
        Vertex[] vertices = GeometryHelper.quad(matrices, 4, 4, 116, 116);
        VertexConsumer.MAIN.consume(vertices, new Resource("whispers", "textures/hints/controls.png"));

        super.render(matrices, mouseX, mouseY, delta);
    }
}
