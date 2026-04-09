package whispers.entities;

import cinnamon.math.collision.Hit;
import cinnamon.model.ModelManager;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.model.ModelRenderer;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import org.joml.Vector3f;
import whispers.items.Fruit;

import java.util.UUID;

public class Bush extends PhysEntity {

    public static final Resource
            model = new Resource("whispers", "models/bush/model.obj"),
            model_fruity = new Resource("whispers", "models/bush_fruit/model2.obj");

    private final ModelRenderer modelFruity;
    private boolean hasFruits = true;
    private int fruitTimer = 0;

    public Bush() {
        super(UUID.randomUUID(), model);
        this.modelFruity = ModelManager.load(model_fruity);
    }

    @Override
    public void tick() {
        super.tick();

        if (fruitTimer > 0 && --fruitTimer == 0)
            hasFruits = true;
    }

    @Override
    protected void renderModel(Camera camera, MatrixStack matrices, float delta) {
        if (hasFruits)
            modelFruity.render(matrices);
        else
            super.renderModel(camera, matrices, delta);
    }

    @Override
    protected void collide(PhysEntity entity, Hit result, Vector3f toMove) {
        if (hasFruits && entity instanceof ThePlayer player && player.getInventory().hasSpace()) {
            hasFruits = false;
            fruitTimer = 600; //30s
            player.giveItem(new Fruit());
        }

        super.collide(entity, result, toMove);
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
