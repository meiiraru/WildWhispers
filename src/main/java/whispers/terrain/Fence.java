package whispers.terrain;

import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.terrain.Terrain;

public class Fence extends Terrain {

    public static final Resource model = new Resource("whispers", "models/fence/model.obj");

    public Fence() {
        super(model, TerrainRegistry.CUSTOM);
    }
}
