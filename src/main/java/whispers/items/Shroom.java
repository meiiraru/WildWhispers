package whispers.items;

import cinnamon.utils.Resource;
import cinnamon.world.items.Item;
import cinnamon.world.items.ItemCategory;
import whispers.entities.ThePlayer;

public class Shroom extends Item {

    public static final Resource model = new Resource("whispers", "models/shroom/model.obj");

    public Shroom() {
        super("Shroom", 1, 1, model);
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.FOOD;
    }

    @Override
    public boolean use() {
        if (!super.use())
            return false;

        //eat the fruit
        ThePlayer player = (ThePlayer) getSource();
        player.eat(FoodType.SHROOM);
        this.setCount(0);

        return true;
    }

    @Override
    public Item copy() {
        return new Shroom();
    }
}
