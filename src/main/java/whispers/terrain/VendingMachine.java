package whispers.terrain;

import cinnamon.math.Maths;
import cinnamon.math.Rotation;
import cinnamon.math.collision.AABB;
import cinnamon.model.GeometryHelper;
import cinnamon.registry.TerrainRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.sound.SoundCategory;
import cinnamon.text.Style;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.items.Item;
import cinnamon.world.items.ItemRenderContext;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.entities.ThePlayer;

import java.util.UUID;

public class VendingMachine extends Terrain {

    private final Item item;
    private final int cost;

    public VendingMachine(Item item, int cost) {
        super(new Resource("whispers", "models/vending_machine/model.obj"), TerrainRegistry.CUSTOM);
        this.item = item;
        this.cost = cost;
        getCollisionMask().setExcludeMask(1, true);
    }

    @Override
    public void render(Camera camera, MatrixStack matrices, float delta) {
        super.render(camera, matrices, delta);


        //render the cost
        matrices.pushMatrix();

        AABB bb = getAABB();
        Vector3f center = bb.getCenter();
        matrices.translate(center.x, bb.maxY() + 0.5f, center.z);

        float s = 1/32f;
        matrices.scale(s, s, s);

        camera.billboard(matrices);
        matrices.rotate(Rotation.Z.rotationDeg(180));

        VertexConsumer.WORLD_MAIN.consume(GeometryHelper.quad(
                matrices, -16, -8, 16, 16
        ), new Resource("whispers", "textures/icons/food.png"));

        matrices.scale(1, 1, -1);
        Text.of("x" + cost)
                .withStyle(Style.EMPTY.outlined(true))
                .render(VertexConsumer.WORLD_MAIN, matrices, 0, 0);

        matrices.popMatrix();

        //render items
        matrices.pushMatrix();
        getTransform().applyTransform(matrices);

        matrices.pushMatrix();
        matrices.translate(-bb.getWidth() / 2f, bb.getHeight(), 0.15f);
        matrices.rotate(Rotation.X.rotationDeg(30f));
        matrices.rotate(Rotation.Z.rotationDeg(-70f));
        matrices.scale(2f);

        item.render(ItemRenderContext.ENTITY, matrices, delta);
        matrices.popMatrix();

        matrices.translate(-bb.getWidth() / 2f - 0.1f, 0.9f, 0.5f);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                matrices.pushMatrix();
                matrices.translate(j * 0.2f, i * 0.4f, 0);
                item.render(ItemRenderContext.ENTITY, matrices, delta);
                matrices.popMatrix();
            }
        }

        matrices.popMatrix();
    }

    @Override
    public boolean interact(Entity entity) {
        if (super.interact(entity))
            return true;

        if (!(entity instanceof ThePlayer player))
            return false;

        if (player.getFood() < cost)
            return false;

        player.setFood(player.getFood() - cost);

        Vector3f dropPos = getAABB().getCenter();
        Vector3f dir = Maths.quatToDir(getTransform().getRot()).rotateY(Math.PI_f);

        ItemEntity itemEntity = new ItemEntity(UUID.randomUUID(), item.copy());
        itemEntity.setPos(dropPos);
        itemEntity.setMotion(dir.mul(0.3f));
        itemEntity.getTerrainCollisionMask().setMask(1, true);
        getWorld().addEntity(itemEntity);

        ((WorldClient) getWorld()).playSound(new Resource("whispers", "sounds/vending_success.ogg"), SoundCategory.TERRAIN, dropPos);

        return true;
    }
}
