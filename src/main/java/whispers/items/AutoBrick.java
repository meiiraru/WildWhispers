package whispers.items;

import cinnamon.world.entity.Entity;
import cinnamon.world.items.BrickItem;
import cinnamon.world.items.Item;
import org.joml.Vector3f;
import whispers.world.TestWorld;

public class AutoBrick extends BrickItem {

    public AutoBrick(int count) {
        super(count);
    }

    @Override
    public Item copy() {
        return new AutoBrick(getCount());
    }

    @Override
    protected Vector3f getThrowDirection(float delta) {
        Entity source = getSource();
        Entity target = ((TestWorld) source.getWorld()).getLookingEnemy();

        if (target == null)
            return super.getThrowDirection(delta);

        Vector3f diff = target.getTransform().getPos().sub(source.getTransform().getPos(), new Vector3f());

        //add offset based on distance to actually hit the target
        float distance = diff.length();
        float offset = distance * 0.15f;
        diff.y += offset;

        return diff.normalize();
    }
}
