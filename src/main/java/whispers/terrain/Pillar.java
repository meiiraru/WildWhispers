package whispers.terrain;

import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.terrain.Terrain;

public class Pillar extends Terrain {

    public static final Resource model = new Resource("whispers", "models/pillar/model.obj");

    public Pillar() {
        super(model, TerrainRegistry.CUSTOM);
    }
}
