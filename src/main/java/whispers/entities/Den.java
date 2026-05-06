package whispers.entities;

import cinnamon.gui.Toast;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.entity.living.LivingEntity;
import cinnamon.world.items.Item;
import cinnamon.world.items.ItemCategory;
import cinnamon.world.world.World;
import org.joml.Vector3f;
import whispers.world.TestWorld;

import java.util.UUID;

public class Den extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/den/model.obj");

    private int food, day;

    public Den() {
        super(UUID.randomUUID(), model);
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().getDay() != day) {
            this.day = getWorld().getDay();
            ((TestWorld) getWorld()).gameover();
        }
    }

    @Override
    public void onAdded(World world) {
        super.onAdded(world);
        updateRequiredFood();
    }

    @Override
    protected Text getHeadText() {
        return food <= 0 ? Text.translated("whispers.den_food_success") :Text.translated("whispers.den_food", food);
    }

    @Override
    public boolean onUse(LivingEntity source) {
        if (source instanceof ThePlayer player) {
            if (getWorld().isNight() || food <= 0) {
                if (food > 0) {
                    ((TestWorld) getWorld()).gameover();
                    return true;
                }

                player.setPos(getPos(0f).add(0, 0.5f, 0));
                player.setHunger(100f);
                player.heal(10);
                player.setFear(0);

                //((TestWorld) getWorld()).genWorld();

                long time = getWorld().getTime();
                int dayLen = getWorld().getDayLength();
                int h = dayLen / 24;
                long timeToAdd = dayLen + (h / 2) - (time % dayLen);
                getWorld().setTime(time + timeToAdd);

                Toast.addToast(Text.translated("whispers.day_count", getWorld().getDay() + 1));
                updateRequiredFood();
                ((TestWorld) getWorld()).eep();
                return true;
            }
        }

        return super.onUse(source);
    }

    @Override
    protected void collide(PhysEntity entity, Hit result, Vector3f toMove) {
        if (food <= 0) {
            super.collide(entity, result, toMove);
            return;
        }

        if (entity instanceof ItemEntity item && item.getItem().getCategory() == ItemCategory.FOOD) {
            item.remove();
            food--;
        } else if (entity instanceof ThePlayer player && player.isHoldingFood()) {
            Item held = player.getHoldingItem();
            held.setCount(held.getCount() - 1);
            food--;
        } else {
            super.collide(entity, result, toMove);
        }
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }

    protected void updateRequiredFood() {
        this.food = ((TestWorld) getWorld()).getRequiredFood();
        this.day = getWorld().getDay();
    }
}
