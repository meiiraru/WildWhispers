package whispers.terrain;

import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.terrain.Terrain;

public class Tombstone extends Terrain {

    private static final Resource[] tombstones = {
            new Resource("whispers", "models/tombstone/model.obj"),
            new Resource("whispers", "models/grave/model.obj"),
    };

    public Tombstone(int index) {
        super(tombstones[index % tombstones.length], TerrainRegistry.CUSTOM);
    }
}
