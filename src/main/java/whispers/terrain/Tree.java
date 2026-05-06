package whispers.terrain;

import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;
import org.joml.Vector3f;
import whispers.particles.LeafParticle;

public class Tree extends Terrain {

    public static final Resource model = new Resource("whispers", "models/tree2/model.obj");

    public Tree() {
        super(model, TerrainRegistry.CUSTOM);
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().getTime() % 20 == 0) {
            LeafParticle leaf = new LeafParticle(600); //30s
            Vector3f pos = getAABB().getRandomPoint();
            pos.y = getAABB().minY() + getAABB().getHeight() * 0.75f;
            leaf.setPos(pos);
            leaf.setMotion((float) Math.random() * 0.2f - 0.1f, -0.1f, (float) Math.random() * 0.2f - 0.1f);
            leaf.getCollisionMask().setExcludeMask(1, true);
            ((WorldClient) getWorld()).addParticle(leaf);
        }
    }
}
