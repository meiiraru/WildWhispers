package whispers.entities;

import cinnamon.animation.Animation;
import cinnamon.math.Maths;
import cinnamon.model.ModelManager;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.model.AnimatedObjRenderer;
import cinnamon.render.model.ModelRenderer;
import cinnamon.utils.Resource;
import cinnamon.world.DamageType;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.living.LocalPlayer;
import cinnamon.world.particle.SmokeParticle;
import cinnamon.world.world.WorldClient;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.items.FoodType;
import whispers.world.TestWorld;

public class ThePlayer extends LocalPlayer {

    public static final Resource MODEL_PATH = new Resource("whispers", "models/fox/model.obj");

    private final ModelRenderer model;

    private float tired = 100, hunger = 20;

    private int onShrooms = 0;

    public ThePlayer() {
        super();
        this.model = ModelManager.load(MODEL_PATH);
        this.getInventory().setSize(1);
        this.getAnimation("idle").setLoop(Animation.Loop.LOOP).play();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getMotion().lengthSquared() > Maths.KINDA_SMALL_NUMBER) {
            if (this.isSprinting()) {
                this.tired -= 2f / 20f;

                this.getAnimation("walk").stop();
                this.getAnimation("run").setLoop(Animation.Loop.LOOP).play();

                if (getWorld().getTime() % 3 == 0) {
                    SmokeParticle particle = new SmokeParticle((int) (Math.random() * 15) + 10, 0xFFFFFFFF);
                    particle.setPos(getPos());
                    ((WorldClient) getWorld()).addParticle(particle);
                }
            } else {
                this.tired -= 0.1f / 20f;

                this.getAnimation("run").stop();
                this.getAnimation("walk").setLoop(Animation.Loop.LOOP).play();
            }
        } else {
            this.getAnimation("walk").stop();
            this.getAnimation("run").stop();
        }

        if (this.onShrooms > 0)
            this.onShrooms--;

        hunger -= 0.3f / 20f;
    }

    @Override
    protected void renderModel(Camera camera, MatrixStack matrices, float delta) {
        if (model != null)
            model.render(matrices);
    }

    @Override
    public void impulse(float left, float up, float forwards) {
        if (riding != null) {
            riding.impulse(left, up, forwards);
            return;
        }

        float l = Math.signum(left);
        float u = this.onGround && up > 0 ? getJumpStrength() : 0f;
        float f = Math.signum(forwards);

        this.impulse.set(l, 0, -f);

        if (impulse.lengthSquared() > 1)
            impulse.normalize();
        impulse.mul(getMoveSpeed());

        impulse.y = u;

        //move the entity in the world camera direction
        this.impulse.rotateY(Math.toRadians(-((TestWorld) getWorld()).camYawLerp));

        //rotate yaw to match impulse direction
        if (l != 0 || f != 0)
            this.rotateTo(0, Maths.dirToRot(impulse).y, 0);
    }

    @Override
    public Animation getAnimation(String name) {
        return ((AnimatedObjRenderer) model).getAnimation(name);
    }

    @Override
    public Vector3f getHandPos(boolean lefty, float delta) {
        Vector3f pos = getPos(delta);
        pos.add(new Vector3f(0f, 0.4f, -0.75f).rotate(getRot(delta)));
        return pos;
    }

    @Override
    public boolean damage(Entity source, DamageType type, int amount, boolean crit) {
        if (super.damage(source, type, amount, crit)) {
            dropItem();
            return true;
        }

        return false;
    }

    @Override
    public float getPickRange() {
        return 0.5f;
    }

    public void eat(FoodType foodType) {
        setHunger(hunger + (foodType == FoodType.PUMPKIN ? 20 : 5));
        setTired(tired + (foodType == FoodType.PUMPKIN ? 10 : 3));
        if (foodType == FoodType.SHROOM)
            onShrooms = 20 * 20; //20s effect
    }

    public float getTired() {
        return tired;
    }

    public void setTired(float tired) {
        this.tired = Maths.clamp(tired, 0, 100);
    }

    public float getHunger() {
        return hunger;
    }

    public void setHunger(float hunger) {
        this.hunger = Maths.clamp(hunger, 0, 100);
    }

    @Override
    protected float getMoveSpeed() {
        return super.getMoveSpeed() * (this.tired > 0 ? 1f : 0.35f);
    }

    @Override
    public void updateMovementFlags(boolean sneaking, boolean sprinting, boolean flying) {
        super.updateMovementFlags(sneaking, sprinting && this.tired > 5, flying);
    }

    public boolean isOnShrooms() {
        return onShrooms > 0;
    }
}
