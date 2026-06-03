package whispers.entities;

import cinnamon.gui.Toast;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.living.LivingEntity;
import cinnamon.world.world.World;
import org.joml.Vector3f;

import java.util.UUID;

public class Den extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/den/model.obj");

    public Den() {
        super(UUID.randomUUID(), model);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onAdded(World world) {
        super.onAdded(world);
        //updateRequiredFood();
    }

    @Override
    protected Text getHeadText() {
        //return food <= 0 ? Text.translated("whispers.den_food_success") :Text.translated("whispers.den_food", food);
        return null;
    }

    @Override
    public boolean onUse(LivingEntity source) {
        if (source instanceof ThePlayer player && player.isFull()) {
            player.setPos(getPos(0f).add(0, 0.5f, 0));
            player.setFood(0);
            player.heal(50);
            player.setFear(0);

            //((TestWorld) getWorld()).genWorld();

            long time = getWorld().getTime();
            int dayLen = getWorld().getDayLength();
            int h = dayLen / 24;
            long timeToAdd = dayLen + (h / 2) - (time % dayLen);
            getWorld().setTime(time + timeToAdd);

            Toast.addToast(Text.translated("whispers.day_count", getWorld().getDay() + 1));
            //updateRequiredFood();
            //((TestWorld) getWorld()).eep();
            return true;
        }

        return super.onUse(source);
    }

    @Override
    protected void collide(PhysEntity entity, Hit result, Vector3f toMove) {
        super.collide(entity, result, toMove);
        /*
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
         */
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }

    protected void updateRequiredFood() {
        //this.food = ((TestWorld) getWorld()).getRequiredFood();
        //this.day = getWorld().getDay();
    }
}
