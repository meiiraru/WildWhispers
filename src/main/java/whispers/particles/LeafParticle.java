package whispers.particles;

import cinnamon.math.collision.AABB;
import cinnamon.math.collision.Collider;
import cinnamon.registry.ParticlesRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.particle.SpriteParticle;
import cinnamon.world.terrain.Terrain;
import whispers.terrain.Tree;

public class LeafParticle extends SpriteParticle {

    public static final Resource texture = new Resource("whispers", "textures/leaf_particle.png");

    private boolean collided;

    public LeafParticle(int lifetime) {
        super(texture, lifetime, 0xFFFFFFFF);
        this.setMotion(0.1f, -0.1f, 0.1f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!collided) {
            AABB aabb = getAABB();
            for (Terrain terrain : world.getTerrains(aabb)) {
                if (terrain instanceof Tree)
                    continue;

                for (Collider<?> terrainColl : terrain.getPreciseCollider()) {
                    if (aabb.intersects(terrainColl)) {
                        collided = true;
                        break;
                    }
                }
            }
        } else {
            getMotion().zero();
        }
    }

    @Override
    public ParticlesRegistry getType() {
        return ParticlesRegistry.OTHER;
    }
}
