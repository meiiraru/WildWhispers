package whispers.entities;

import cinnamon.math.Maths;
import cinnamon.math.collision.Hit;
import cinnamon.model.ModelManager;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.model.ModelRenderer;
import cinnamon.sound.SoundCategory;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.world.WorldClient;
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
            this.setFruits(true);
    }

    @Override
    protected void renderModel(Camera camera, MatrixStack matrices, float delta) {
        if (hasFruits)
            modelFruity.render(matrices);
        else
            super.renderModel(camera, matrices, delta);
    }

    @Override
    protected void collideEntity(PhysEntity entity, Hit result, Vector3f toMove) {
        if (hasFruits && entity instanceof ThePlayer player && player.getInventory().hasSpace()) {
            hasFruits = false;
            fruitTimer = 600; //30s
            player.giveItem(new Fruit());
            ((WorldClient) getWorld()).playSound(ItemEntity.PICK_UP_SOUND, SoundCategory.ENTITY, player.getTransform().getPos()).pitch(Maths.range(0.85f, 1.15f));
        }

        super.collideEntity(entity, result, toMove);
    }

    public void setFruits(boolean hasFruits) {
        this.hasFruits = hasFruits;
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
