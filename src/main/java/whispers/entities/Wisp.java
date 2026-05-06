package whispers.entities;

import cinnamon.math.Maths;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.utils.Colors;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.light.Light;
import cinnamon.world.light.PointLight;
import cinnamon.world.world.World;
import cinnamon.world.world.WorldClient;
import org.joml.Vector3f;
import whispers.particles.WispParticle;

import java.util.UUID;

public class Wisp extends PhysEntity {

    private final Light light;
    private final boolean force;

    public Wisp(boolean force) {
        super(UUID.randomUUID(), null);
        this.force = force;
        this.setGravity(0f);
        //addRenderFeature((source, camera, matrices, delta) -> {
        //    matrices.pushMatrix();
        //    matrices.translate(getPos(delta));
        //    float r = 0.05f;
        //    VertexConsumer.WORLD_MAIN_EMISSIVE.consume(GeometryHelper.box(matrices, -r, -r, -r, r, r, r, Colors.BLUE.argb));
        //    matrices.popMatrix();
        //});
        this.light = new PointLight().falloff(1f).glareSize(3f).color(Colors.BLUE.rgb).castsShadows(false).intensity(0f);
    }

    @Override
    public void onAdded(World world) {
        super.onAdded(world);
        ((WorldClient) world).addLight(this.light);
    }

    @Override
    public void remove() {
        super.remove();
        ((WorldClient) getWorld()).removeLight(this.light);
    }

    @Override
    public void tick() {
        super.tick();

        Vector3f pos = getPos();
        moveTo(pos.x, pos.y + (float) Math.sin(getWorld().getTime() * 0.05f) * 0.01f, pos.z);

        if (isVisible() && getWorld().getTime() % 10 == 0) {
            WispParticle particle = new WispParticle(40);
            Vector3f ppos = new Vector3f(getPos());
            ppos.add((float) (Math.random() * 0.5f - 0.25f), (float) (Math.random() * 0.5f - 0.25f), (float) (Math.random() * 0.5f - 0.25f));
            particle.setPos(ppos);
            ((WorldClient) getWorld()).addParticle(particle);
        }

        Vector3f playerPos = ((WorldClient) getWorld()).player.getPos();
        float distance = playerPos.distance(getPos());
        float delta = distance > 100 ? 0f : (force ? 1f : (world.isNight() ? Maths.clamp((distance - 5f) / 5f, 0f, 1f) : 0f));
        light.intensity(5f * delta);
        light.glareIntensity(delta);
    }

    @Override
    public void render(Camera camera, MatrixStack matrices, float delta) {
        super.render(camera, matrices, delta);
        this.light.pos(getPos(delta));
    }

    public boolean isVisible() {
        return force || (getWorld().isNight() && light.getIntensity() > 0f);
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
