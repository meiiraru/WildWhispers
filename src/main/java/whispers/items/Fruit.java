package whispers.items;

import cinnamon.utils.Resource;
import cinnamon.world.items.Item;
import whispers.entities.ThePlayer;

public class Fruit extends Item {

    public static final Resource model = new Resource("whispers", "models/fruit/model.obj");

    public Fruit() {
        super("Fruit", 1, 1, model);
    }

    @Override
    public boolean use() {
        if (!super.use())
            return false;

        //eat the fruit
        ThePlayer player = (ThePlayer) getSource();
        player.eat(FoodType.FRUIT);
        this.setCount(0);

        return true;
    }

    @Override
    public Item copy() {
        return new Fruit();
    }
}
