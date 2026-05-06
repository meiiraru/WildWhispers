package whispers.particles;

import cinnamon.registry.ParticlesRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.utils.Resource;
import cinnamon.world.particle.SpriteParticle;

public class WispParticle extends SpriteParticle {

    public WispParticle(int lifetime) {
        super(new Resource("whispers", "textures/fire.png"), lifetime, 0xFFFFFFFF);
        this.setEmissive(true);
    }

    @Override
    public void render(Camera camera, MatrixStack matrices, float delta) {
        setScale(1f - ((getAge() + delta) / getLifetime()));
        super.render(camera, matrices, delta);
    }

    @Override
    public int getCurrentFrame() {
        return getAge() % getFrameCount();
    }

    @Override
    public int getFrameCount() {
        return 8;
    }

    @Override
    public ParticlesRegistry getType() {
        return ParticlesRegistry.OTHER;
    }
}
