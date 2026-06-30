package whispers.entities;

import cinnamon.animation.Animation;
import cinnamon.math.Maths;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.sound.SoundCategory;
import cinnamon.utils.Resource;
import cinnamon.world.DamageType;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.living.LivingEntity;
import cinnamon.world.world.WorldClient;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.world.TestWorld;

import java.util.UUID;

public class Bird extends LivingEntity {

    public static final Resource MODEL_PATH = new Resource("whispers", "models/bird/model.obj");

    private final Vector3f spawnPos = new Vector3f(0, 0, 0);
    private final Vector3f targetPos = new Vector3f(0, 0, 0);

    private int ticks;
    private int diveDelay;

    private boolean chasing = false;

    public Bird(float spawnX, float spawnY, float spawnZ) {
        super(UUID.randomUUID(), MODEL_PATH, 0.25f, 7, 0);
        this.spawnPos.set(spawnX, spawnY, spawnZ);
        this.setGravity(0f);
        this.getTerrainCollisionMask().setExcludeMask(-1); //exclude all terrain collisions
        this.getAnimation("idle").setLoop(Animation.Loop.LOOP).play();
    }

    @Override
    public void tick() {
        super.tick();

        ticks++;

        ThePlayer player = (ThePlayer) ((TestWorld) getWorld()).player;

        if (--diveDelay <= 0 && getWorld().isNight() && player.getFood() > 20 && player.getTransform().getPos().distanceSquared(getTransform().getPos()) < 15f * 15f) {
            //dive towards player
            targetPos.set(player.getTransform().getPos());

            boolean wasChasing = chasing;
            chasing = true;
            if (!wasChasing)
                ((WorldClient) getWorld()).playSound(new Resource("whispers", "sounds/eagle.ogg"), SoundCategory.ENTITY, getTransform().getPos());
        } else {
            chasing = false;

            //wander into a circular continuous motion in a radius from the spawn pos
            float radius = 5f;
            float speed = 0.01f;
            float angle = ticks * speed;
            float x = spawnPos.x + Math.cos(angle) * radius;
            float z = spawnPos.z + Math.sin(angle) * radius;
            targetPos.set(x, spawnPos.y, z);
        }

        //move towards target pos by our speed
        float speed = getMoveSpeed();
        Vector3f pos = getTransform().getPos();

        //if we are close enough to the target pos, just set our position to it
        if (pos.distanceSquared(targetPos) < Maths.KINDA_SMALL_NUMBER) {
            this.moveTo(targetPos.x, targetPos.y, targetPos.z);
            return;
        }

        if (pos.distanceSquared(targetPos) <= speed * speed) {
            this.moveTo(targetPos.x, targetPos.y, targetPos.z);
            Vector3f lookDir = getTransform().getPos().sub(oPos, new Vector3f()).normalize();
            this.rotateTo(Maths.dirToQuat(lookDir).rotateY(Math.PI_f));
            return;
        }

        Vector3f toMove = targetPos.sub(oPos, new Vector3f()).normalize(speed);
        this.moveTo(pos.x + toMove.x, pos.y + toMove.y, pos.z + toMove.z);

        //look at the current direction of motion
        Vector3f lookDir = getTransform().getPos().sub(oPos, new Vector3f()).normalize();
        this.rotateTo(Maths.dirToQuat(lookDir).rotateY(Math.PI_f));
    }

    @Override
    protected void collideEntity(PhysEntity entity, Hit result, Vector3f toMove) {
        if (entity instanceof ThePlayer player) {
            if (player.damage(this, DamageType.MELEE, 10, false)) {
                ((WorldClient) getWorld()).playSound(new Resource("whispers", "sounds/bite.ogg"), SoundCategory.ENTITY, getTransform().getPos());
                diveDelay = 15 * 20;
            }
        }

        super.collideEntity(entity, result, toMove);
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
