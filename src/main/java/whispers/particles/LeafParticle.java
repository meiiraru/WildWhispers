package whispers.particles;

import cinnamon.registry.ParticlesRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.particle.SpriteParticle;

public class LeafParticle extends SpriteParticle {

    public static final Resource texture = new Resource("whispers", "textures/leaf_particle.png");

    public LeafParticle(int lifetime) {
        super(texture, lifetime, 0xFFFFFFFF);
        this.setMotion(0.1f, -0.1f, 0.1f);
    }

    @Override
    public void tick() {
        super.tick();
        if (collideTerrain())
            getMotion().zero();
    }

    @Override
    public ParticlesRegistry getType() {
        return ParticlesRegistry.OTHER;
    }
}
