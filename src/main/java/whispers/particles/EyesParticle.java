package whispers.particles;

import cinnamon.registry.ParticlesRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.particle.SpriteParticle;

public class EyesParticle extends SpriteParticle {

    public static final Resource texture = new Resource("whispers", "textures/eyes_particle.png");

    public EyesParticle(int lifetime) {
        super(texture, lifetime, 0xFFFFFFFF);
        this.setEmissive(true);
    }

    @Override
    public ParticlesRegistry getType() {
        return ParticlesRegistry.OTHER;
    }
}
