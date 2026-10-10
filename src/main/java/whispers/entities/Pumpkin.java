package whispers.entities;

import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.PhysEntity;
import whispers.items.FoodType;

import java.util.UUID;

public class Pumpkin extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/pumpkin/model.obj");
    public static final int MAX_AGE = 1000;

    private int age;

    public Pumpkin(int initialAge) {
        super(UUID.randomUUID(), model);
        this.age = Math.min(initialAge, MAX_AGE);
    }

    @Override
    public void tick() {
        super.tick();
        if (age > 0)
            age--;

        this.scaleTo(1f - (age / (float) MAX_AGE));
    }

    @Override
    public boolean onUse(Entity source, Hit hit) {
        if (!(source instanceof ThePlayer player))
            return super.onUse(source, hit);

        if (!canBeEaten())
            return false;

        //eat the pumpkin
        player.eat(FoodType.PUMPKIN);
        remove();
        return true;
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }

    public boolean canBeEaten() {
        return age <= 0;
    }
}
