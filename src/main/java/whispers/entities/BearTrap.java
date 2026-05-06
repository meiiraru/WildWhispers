package whispers.entities;

import cinnamon.animation.Animation;
import cinnamon.math.collision.Hit;
import cinnamon.registry.EntityRegistry;
import cinnamon.sound.SoundCategory;
import cinnamon.utils.Resource;
import cinnamon.world.DamageType;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.entity.living.LivingEntity;
import cinnamon.world.entity.living.Player;
import cinnamon.world.items.Item;
import cinnamon.world.world.WorldClient;
import org.joml.Vector3f;

import java.util.UUID;

public class BearTrap extends PhysEntity {

    public static final Resource model = new Resource("whispers", "models/bear_trap/model.obj");
    private static final int snapAnim = 60; //3s

    private Entity trappedEntity;
    private int snapTime, snapDelay;

    public BearTrap() {
        super(UUID.randomUUID(), model);
    }

    @Override
    public void tick() {
        super.tick();

        if (snapTime > 0) {
            if (--snapTime == 0) {
                trappedEntity = null;
                snapDelay = 20; //1s
            } else if (snapTime == snapAnim) {
                getAnimation("snap").stop();
                getAnimation("open").setLoop(Animation.Loop.ONCE).play();
                ((WorldClient) getWorld()).playSound(new Resource("whispers", "sounds/beartrap_set.ogg"), SoundCategory.ENTITY, getPos());
            }
        }

        if (snapDelay > 0)
            snapDelay--;

        if (trappedEntity != null) {
            if (trappedEntity instanceof ItemEntity item) {
                trappedEntity.moveTo(pos.x, pos.y + 0.1f, pos.z);
                item.setPickUpDelay(100);
            } else {
                trappedEntity.moveTo(getPos());
            }
        }
    }

    @Override
    protected void collide(PhysEntity entity, Hit result, Vector3f toMove) {
        if (trappedEntity != null || snapDelay > 0) {
            super.collide(entity, result, toMove);
            return;
        }

        Entity target;

        if (entity instanceof LivingEntity le) {
            Item holding = le.getHoldingItem();
            if (holding == null) {
                snapTime = 60 + snapAnim; //3s + anim time
                target = le;
            } else {
                target = le.dropItem(-1);
            }

            le.damage(null, DamageType.TERRAIN, 5, false);
        } else if (entity instanceof ItemEntity itemEntity) {
            target = itemEntity;
        } else {
            super.collide(entity, result, toMove);
            return;
        }

        if (target instanceof ItemEntity item) {
            item.setPickUpDelay(100);
            item.setGravity(0f);
            item.setAge(-1);
            snapTime = -1;
        }

        this.trappedEntity = target;
        this.getAnimation("snap").setLoop(Animation.Loop.HOLD).play();
        ((WorldClient) getWorld()).playSound(new Resource("whispers", "sounds/beartrap_setoff.ogg"), SoundCategory.ENTITY, getPos());
    }

    @Override
    public boolean shouldRenderOutline() {
        return trappedEntity != null && trappedEntity instanceof Player;
    }

    @Override
    public int getOutlineColor() {
        return 0xFFFF0000;
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
