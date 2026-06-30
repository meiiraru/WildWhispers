package whispers.entities;

import cinnamon.animation.Animation;
import cinnamon.math.Maths;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.utils.Resource;
import cinnamon.world.DamageType;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.living.LivingEntity;
import cinnamon.world.entity.living.Player;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.world.TestWorld;

import java.util.UUID;

public class Dog extends LivingEntity {

    public static final Resource MODEL_PATH = new Resource("whispers", "models/wolf/model.obj");

    private static final Vector3f DIMENSIONS = new Vector3f(0.6f, 1.8f, 0.6f);

    private int wanderCooldown = 0;
    private final Vector3f wanderTarget = new Vector3f();

    public Dog() {
        super(UUID.randomUUID(), MODEL_PATH, 0.5f, 15, 0);
        this.getAnimation("idle").setLoop(Animation.Loop.LOOP).play();
    }

    @Override
    public void tick() {
        super.tick();

        Player player = ((TestWorld) getWorld()).player;
        float distance = player.getTransform().getPos().distanceSquared(getTransform().getPos());
        if (distance <= 8*8 && distance > 0.1f) {
            this.lookAt(player.getTransform().getPos());
            this.impulse(0, 0, 1);
            this.getAnimation("run").setLoop(Animation.Loop.LOOP).play();
            this.getAnimation("walk").stop();
            wanderCooldown = 0;
        } else {
            this.getAnimation("run").stop();

            if (--wanderCooldown <= 0) {
                wanderTarget.set(getTransform().getPos()).add(Maths.range(-5, 5), 0, Maths.range(-5, 5));
                wanderCooldown = Maths.range(3*20, 5*20);
            }

            float distanceToTarget = getTransform().getPos().distanceSquared(wanderTarget);
            if (distanceToTarget > 0.1f) {
                this.lookAt(wanderTarget);
                this.impulse(0, 0, 1f);
                this.getAnimation("walk").setLoop(Animation.Loop.LOOP).play();
            } else {
                this.getAnimation("walk").stop();
            }
        }
    }

    @Override
    protected void collideEntity(PhysEntity entity, Hit result, Vector3f toMove) {
        super.collideEntity(entity, result, toMove);

        if (entity instanceof Player player) {
            player.damage(this, DamageType.MELEE, 10, false);
        }
    }

    @Override
    public void calculateBounds() {
        aabb.set(transform.getPos());
        float w = Math.max(DIMENSIONS.x, DIMENSIONS.z) * 0.5f;
        float y = model.getAABB().getHeight(); //Math.min(, DIMENSIONS.y);
        aabb.inflate(w, 0, w, w, y, w);
        aabb.scaleAnchorBottom(transform.getScale());
    }

    @Override
    protected void onDeath() {
        ((TestWorld) getWorld()).score += 30;
        super.onDeath();
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
