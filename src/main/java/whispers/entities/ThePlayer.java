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
import cinnamon.world.items.Item;
import cinnamon.world.items.ItemCategory;
import cinnamon.world.particle.SmokeParticle;
import cinnamon.world.world.WorldClient;
import org.joml.Math;
import org.joml.Vector3f;
import whispers.items.FoodType;
import whispers.world.TestWorld;

public class ThePlayer extends LocalPlayer {

    public static final Resource MODEL_PATH = new Resource("whispers", "models/fox/model.obj");

    private final ModelRenderer model;

    private boolean baby = false;

    private float fear = 0;
    private int food = 0;
    private int foodEaten = 0;

    private float stamina = 100f;
    private int staminaInUse = 0;

    private int onShrooms = 0;

    private EventType currentEvent = null;
    private int eventTimer = 0;

    private int tpCooldown = 0;

    public ThePlayer(boolean baby) {
        super();
        this.model = ModelManager.load(MODEL_PATH);
        this.getInventory().setSize(1);
        this.getAnimation("idle").setLoop(Animation.Loop.LOOP).play();
        this.setBaby(baby);

        /*
        addRenderFeature((source, camera, matrices, delta) -> {
            if (currentEvent == null || eventTimer <= 0)
                return;

            matrices.pushMatrix();

            matrices.translate(getPos(delta));
            matrices.translate(0, 2 + Math.sin((getWorld().getTime() + delta) * 0.1f) * 0.25f, 0);
            camera.billboard(matrices);
            matrices.rotate(Rotation.Z.rotationDeg(180f));

            float s = 0.3f;
            VertexConsumer.WORLD_MAIN.consume(GeometryHelper.quad(matrices, -s, -s, s+s, s+s), new Resource("whispers", "textures/speech_bubble.png"));
            VertexConsumer.WORLD_MAIN.consume(GeometryHelper.quad(matrices, -s, -s, s+s, s+s), currentEvent.icon);

            matrices.popMatrix();
        });
        */
    }

    @Override
    public void tick() {
        super.tick();

        if (eventTimer > 0 && --eventTimer <= 0)
            currentEvent = null;

        if (this.getMotion().lengthSquared() > Maths.KINDA_SMALL_NUMBER) {
            if (this.isSprinting()) {
                this.getAnimation("walk").stop();
                this.getAnimation("run").setLoop(Animation.Loop.LOOP).play();

                if (getWorld().getTime() % 3 == 0) {
                    SmokeParticle particle = new SmokeParticle((int) (Math.random() * 15) + 10, 0xFFFFFFFF);
                    particle.setPos(getTransform().getPos());
                    ((WorldClient) getWorld()).addParticle(particle);
                }

                staminaInUse = 2*20;
                setStamina(stamina - (isBaby() ? 2 : 1));
            } else {
                this.getAnimation("run").stop();
                this.getAnimation("walk").setLoop(Animation.Loop.LOOP).play();
            }
        } else {
            this.getAnimation("walk").stop();
            this.getAnimation("run").stop();
        }

        if (this.onShrooms > 0)
            this.onShrooms--;

        //setHunger(hunger - 0.3f / 20f);

        if (fear > 0)
            fear--;

        if (tpCooldown > 0)
            tpCooldown--;

        if (--staminaInUse <= 0)
            setStamina(stamina + (100f / 5f / 20f));

        //if (hunger <= 0 && !isDead())
        //    kill();
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
    protected float getMoveSpeed() {
        return super.getMoveSpeed() * (isBaby() ? 0.9f : 1f);
    }

    @Override
    public Animation getAnimation(String name) {
        return ((AnimatedObjRenderer) model).getAnimation(name);
    }

    @Override
    public Vector3f getHandPos(boolean lefty, float delta) {
        Vector3f pos = getPos(delta);
        if (isBaby())
            pos.add(new Vector3f(0f, 0.2f, -0.37f).rotate(getRot(delta)));
        else
            pos.add(new Vector3f(0f, 0.4f, -0.75f).rotate(getRot(delta)));
        return pos;
    }

    @Override
    public boolean damage(Entity source, DamageType type, int amount, boolean crit) {
        if (super.damage(source, type, amount, crit)) {
            //dropItem(-1, true);

            //if (getFood() <= 0) {
            //    kill();
            //    ((TestWorld) getWorld()).gameover();
            //}

            setFood(food - 1);
            showEvent(EventType.HURT, 3*20);

            ((TestWorld) getWorld()).score -= 50;

            return true;
        }

        return false;
    }

    @Override
    public boolean heal(int amount) {
        if (super.heal(amount)) {
            showEvent(EventType.HEAL, 5*20);
            return true;
        }

        return false;
    }

    @Override
    protected void spawnHealthChangeParticle(int amount, boolean crit) {
        //super.spawnHealthChangeParticle(amount, crit);
    }

    @Override
    public float getPickRange() {
        return 0.5f;
    }

    public void eat(FoodType foodType) {
        setFood(food + 1);
        if (foodType == FoodType.SHROOM)
            onShrooms = 10 * 20; //10s effect
        setStamina(stamina + 10f);
        ((TestWorld) getWorld()).score += 10;
        foodEaten++;
        if (isBaby() && foodEaten >= 15)
            setBaby(false);
    }

    public int getFood() {
        return food;
    }

    public boolean isFull() {
        return false;
    }

    public void setFood(int food) {
        this.food = Math.max(food, 0);
        //showEvent(EventType.EAT, 5*20);
    }

    public boolean isOnShrooms() {
        return onShrooms > 0;
    }

    public boolean isHoldingFood() {
        Item item = getHoldingItem();
        return item != null && item.getCategory() == ItemCategory.FOOD;
    }

    public boolean hasFear() {
        return fear > 0;
    }

    public void setFear(int fearTicks) {
        float prevFear = this.fear;
        this.fear = fearTicks;
        if (prevFear < this.fear)
            showEvent(EventType.FEAR, fearTicks);
    }

    public void setStamina(float stamina) {
        this.stamina = Maths.clamp(stamina, 0f, 100f);
    }

    public float getStamina() {
        return stamina;
    }

    public void showEvent(EventType type, int duration) {
        if (this.currentEvent != null && this.currentEvent.ordinal() < type.ordinal())
            return;

        this.currentEvent = type;
        this.eventTimer = duration;
    }

    public boolean isOnTeleportCooldown() {
        return tpCooldown > 0;
    }

    public void setTeleportCooldown(int ticks) {
        this.tpCooldown = ticks;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
        this.getTransform().setScale(baby ? 0.5f : 1f);
    }

    public boolean isBaby() {
        return baby;
    }

    @Override
    public void updateMovementFlags(boolean sneaking, boolean sprinting, boolean flying) {
        super.updateMovementFlags(sneaking, stamina > 0f && sprinting, flying);
    }

    public enum EventType {
        HEAL(new Resource("whispers", "textures/icons/heart.png")),
        HURT(new Resource("whispers", "textures/icons/broken_heart.png")),
        FEAR(new Resource("whispers", "textures/icons/fear.png")),
        EAT(new Resource("whispers", "textures/icons/food.png")),
        HUNGER(new Resource("whispers", "textures/icons/hunger.png"));

        public final Resource icon;

        EventType(Resource resource) {
            this.icon = resource;
        }
    }
}
