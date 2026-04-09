package whispers.entities;

import cinnamon.gui.Toast;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.entity.living.LivingEntity;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.world.TestWorld;

import java.util.UUID;

public class Den extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/den/model.obj");

    private int food = 0;

    public Den() {
        super(UUID.randomUUID(), model);
    }

    @Override
    protected Text getHeadText() {
        return Text.of("Food: " + food);
    }

    @Override
    public boolean onUse(LivingEntity source) {
        if (source instanceof ThePlayer player) {
            if (((TestWorld) getWorld()).isNight() || player.getTired() <= 15) {
                player.setPos(getPos(0f).add(0, 0.5f, 0));

                int fruitsToTake = (int) Math.ceil((100f - player.getHunger()) / 5f);
                int fruitsTaken = Math.min(food, fruitsToTake);
                food -= fruitsTaken;
                player.setHunger(player.getHunger() + fruitsTaken * 5);

                if (player.getHunger() >= 100) {
                    player.setTired(100);
                    player.setHealth(Math.min(player.getHealth() + 10, player.getMaxHealth()));
                }

                ((TestWorld) getWorld()).genWorld();

                long time = getWorld().getTime();
                long timeToAdd = 25000L - (time % 24000L);
                getWorld().setTime(time + timeToAdd);

                Toast.addToast("Day " + (getWorld().getDay() + 1));
                return true;
            }
        }

        return super.onUse(source);
    }

    @Override
    protected void collide(PhysEntity entity, Hit result, Vector3f toMove) {
        if (!(entity instanceof ItemEntity item)) {
            super.collide(entity, result, toMove);
            return;
        }

        food++;
        item.remove();
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
