package whispers.entities;

import cinnamon.registry.EntityRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.living.LivingEntity;
import whispers.items.FoodType;

import java.util.UUID;

public class Pumpkin extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/pumpkin/model.obj");

    public Pumpkin() {
        super(UUID.randomUUID(), model);
    }

    @Override
    public boolean onUse(LivingEntity source) {
        if (!(source instanceof ThePlayer player))
            return super.onUse(source);

        //eat the pumpkin
        player.eat(FoodType.PUMPKIN);
        remove();
        return true;
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
