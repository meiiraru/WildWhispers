package whispers.world;

import cinnamon.registry.MaterialRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.WorldRenderer;
import cinnamon.render.shader.PostProcess;
import cinnamon.sound.SoundCategory;
import cinnamon.utils.Resource;
import cinnamon.utils.UIHelper;
import cinnamon.world.Abilities;
import cinnamon.world.DamageType;
import cinnamon.world.WorldObject;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;
import cinnamon.world.worldgen.TerrainGenerator;
import org.joml.Math;
import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import whispers.entities.BearTrap;
import whispers.entities.Bush;
import whispers.entities.Den;
import whispers.entities.Pumpkin;
import whispers.entities.ThePlayer;
import whispers.io.LocalInput;
import whispers.items.Shroom;
import whispers.particles.EyesParticle;
import whispers.screens.SmallHud;
import whispers.screens.WhisperDeath;
import whispers.terrain.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestWorld extends WorldClient {

    public float camYaw = 135f, camYawLerp = 135f, camZoom = 10f, camZoomLerp = 10f;

    private final List<WorldObject> objects = new ArrayList<>();
    private final Random rand = new Random(3726L);

    public TestWorld() {
        this.movement = new LocalInput();
        this.enableDebugKeys = false;
        this.chatScreen = () -> null;
        this.deathScreen = WhisperDeath::new;
    }

    @Override
    protected void tempLoad() {
        this.hud = new SmallHud();
        this.hud.init();

        playSound(new Resource("whispers", "sounds/wind-rustling-grass.ogg"), SoundCategory.AMBIENT, new Vector3f())
                .loop(true)
                .distance(64f);

        int r = 32;
        TerrainGenerator.fill(this, -r, 0, -r, r, 0, r, MaterialRegistry.GRASS);
        setTime(125L);

        //add den
        Den den = new Den();
        den.setPos(0.5f, 1.5f, 0.5f);
        addEntity(den);

        genWorld();
    }

    public void genWorld() {
        for (WorldObject object : objects) {
           if (object instanceof Entity entity)
                entity.remove();
             else if (object instanceof Terrain terrain)
                removeTerrain(terrain);
        }

        float r = 32f;

        //add trees
        for (int i = 0; i < 20; i++) {
            Tree tree = new Tree();
            tree.setPos((rand.nextFloat() * 2f - 1f) * r, 1f, (rand.nextFloat() * 2f - 1f) * r);
            tree.setRotation((byte) rand.nextInt(4));
            addTerrain(tree);
            objects.add(tree);
        }

        //add bear traps
        for (int i = 0; i < 15; i++) {
            BearTrap trap = new BearTrap();
            trap.setPos((rand.nextFloat() * 2f - 1f) * r, 1.5f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(trap);
            objects.add(trap);
        }

        //add bushes
        for (int i = 0; i < 15; i++) {
            Bush bush = new Bush();
            bush.setPos((rand.nextFloat() * 2f - 1f) * r, 1.5f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(bush);
            objects.add(bush);
        }

        //pumpkins
        for (int i = 0; i < 3; i++) {
            Pumpkin pumpkin = new Pumpkin();
            pumpkin.setPos((rand.nextFloat() * 2f - 1f) * r, 1.5f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(pumpkin);
            objects.add(pumpkin);
        }

        //shrooms
        for (int i = 0; i < 5; i++) {
            ItemEntity item = new ItemEntity(UUID.randomUUID(), new Shroom());
            item.setPos((rand.nextFloat() * 2f - 1f) * r, 3f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(item);
            objects.add(item);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (isNight()) {
            if (getTime() % 10 == 0) {
                EyesParticle particle = new EyesParticle((int) (Math.random() * 100) + 200);
                Vector3f pos = player.getPos(0f);
                pos.add((float) Math.random() * 64f - 32f, (float) Math.random() * 5f + 1, (float) Math.random() * 64f - 32f);
                particle.setPos(pos);
                addParticle(particle);
            }

            if (getTime() % 20 == 0) {
                player.damage(null, DamageType.GOD, 5, false);
            }
        }
    }

    @Override
    protected void postWorldRender(MatrixStack matrices, float delta) {
        super.postWorldRender(matrices, delta);
        //PostProcess.apply(PostProcess.VINTAGE);
        if (((ThePlayer) player).isOnShrooms())
            PostProcess.apply(PostProcess.TRIPPY);
    }

    @Override
    protected void updateSky(Camera camera, float worldTime) {
        super.updateSky(camera, worldTime * 4f);
    }

    @Override
    protected void updateCamera(Camera sourceCamera, Entity camEntity, int cameraMode, float delta) {
        super.updateCamera(sourceCamera, camEntity, this.cameraMode = 1, delta);

        float d = UIHelper.tickDelta(0.8f);
        camYawLerp = Math.lerp(camYawLerp, camYaw, d);
        camZoomLerp = Math.lerp(camZoomLerp, camZoom, d);

        Vector3f camPos = camEntity.getPos(delta);
        WorldRenderer.camera.setPos(camPos.x, camPos.y, camPos.z);
        WorldRenderer.camera.setRot(45f, camYawLerp, 0f);
        WorldRenderer.camera.move(0, 0, camZoomLerp, true);
    }

    @Override
    public void keyPress(int key, int scancode, int action, int mods) {
        super.keyPress(key, scancode, action, mods);

        if (action == GLFW.GLFW_RELEASE)
            return;

        switch (key) {
            case GLFW.GLFW_KEY_A -> this.camYaw += 45f;
            case GLFW.GLFW_KEY_D -> this.camYaw -= 45f;
            case GLFW.GLFW_KEY_W -> this.camZoom = Math.max(this.camZoom - 1f, 3f);
            case GLFW.GLFW_KEY_S -> this.camZoom = Math.min(this.camZoom + 1f, 15f);

            case GLFW.GLFW_KEY_X -> this.player.useAction();
            case GLFW.GLFW_KEY_C -> this.player.dropItem();
            case GLFW.GLFW_KEY_G -> spawnDebugWeapons();
        }
    }

    @Override
    public void respawn(boolean init) {
        this.player = new ThePlayer();
        this.player.setPos(0.5f, 1.5f, 0.5f);
        this.player.getAbilities().set(Abilities.Ability.CAN_BUILD, false);
        this.addEntity(this.player);
    }

    public boolean isNight() {
        return (getTime() * 4) % 24000L >= 12000L;
    }
}
