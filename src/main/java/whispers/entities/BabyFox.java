package whispers.entities;

import cinnamon.animation.Animation;
import cinnamon.registry.EntityRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import whispers.world.TestWorld;

import java.util.UUID;

public class BabyFox extends PhysEntity {

    public static final Resource MODEL_PATH = new Resource("whispers", "models/fox/model.obj");

    private int jumpTicks = (int) (Math.random() * 100);

    public BabyFox() {
        super(UUID.randomUUID(), MODEL_PATH);
        this.getAnimation("idle").setLoop(Animation.Loop.LOOP).play();
        this.getTransform().setScale(0.5f);
    }

    @Override
    public void tick() {
        super.tick();

        lookAt(((TestWorld) getWorld()).player.getPos(1f));

        if (--jumpTicks <= 0) {
            this.impulse.set(0f, 0.2f, 0f);
            jumpTicks = (int) (Math.random() * 100);
        }
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
