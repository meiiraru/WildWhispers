package whispers.world;

import cinnamon.events.Await;
import cinnamon.model.material.Material;
import cinnamon.model.material.MaterialTexture;
import cinnamon.parsers.BBModelTerrainLoader;
import cinnamon.registry.TerrainRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.WorldRenderer;
import cinnamon.render.shader.PostProcess;
import cinnamon.render.shader.Shader;
import cinnamon.render.texture.Texture;
import cinnamon.sound.SoundCategory;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.utils.UIHelper;
import cinnamon.world.Abilities;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.entity.misc.Spawner;
import cinnamon.world.items.BrickItem;
import cinnamon.world.particle.RainParticle;
import cinnamon.world.sky.SkyColors;
import cinnamon.world.terrain.Rose;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import whispers.entities.*;
import whispers.io.LocalInput;
import whispers.items.AutoBrick;
import whispers.items.Shroom;
import whispers.screens.PauseMenu;
import whispers.screens.SmallHud;
import whispers.screens.WhisperDeath;
import whispers.terrain.Tree;
import whispers.terrain.VendingMachine;

import java.util.UUID;

public class TestWorld extends WorldClient {

    public float camYaw = 135f, camYawLerp = 135f, camZoom = 7.5f, camZoomLerp = 10f;

    private boolean firstNight = true;
    private float downpour = 0f;
    private int downpourTime = 0;

    private Den den;

    public int score = 0;
    public int lives = 3;

    public TestWorld() {
        this.movement = new LocalInput();
        this.enableDebugKeys = false;
        this.chatScreen = () -> null;
        this.deathScreen = WhisperDeath::new;
        this.pauseScreen = PauseMenu::new;
    }

    @Override
    protected void levelLoad() {
        this.hud = new SmallHud();
        this.hud.init();
        this.hud.setFade(true, 0, 0x000000);
        scheduledTicks.add(() -> new Await(30, () -> {
            this.hud.setFade(false, 60, 0x000000);

            ((SmallHud) hud).displayControls(25*20);
            ((SmallHud) hud).addDialog(10*20, Text.translated("whispers.intro"));
            new Await(10*20, () -> ((SmallHud) hud).addDialog(10*20, Text.translated("whispers.intro2")));
        }));

        this.dayLength = 5*60*20;
        this.nightStart = dayLength / 2;

        playSound(new Resource("whispers", "sounds/wind-rustling-grass.ogg"), SoundCategory.AMBIENT, new Vector3f())
                .loop(true)
                .distance(64f);

        setTimeMinutes(6*60+30); //6:30AM

        Material grass = new Material("grass");
        grass.setAlbedo(new MaterialTexture(new Resource("whispers", "textures/grass.png"), Texture.TextureParams.MIPMAP));

        try {
            BBModelTerrainLoader.load(new Resource("whispers", "terrain.bbmodel"), 1f, (id, transform) -> {
                Terrain t = null;
                Entity e = null;
                switch (id) {
                    case "floor" -> {
                        t = TerrainRegistry.BOX.getFactory().get();
                        t.setMaterial(grass);
                    }
                    case "tree" -> t = new Tree();

                    case "rose", "rose:red" -> {
                        t = new Rose();
                        ((Rose) t).setVariant(Rose.Variant.RED);
                    }
                    case "rose:white" -> {
                        t = new Rose();
                        ((Rose) t).setVariant(Rose.Variant.WHITE);
                    }
                    case "rose:pink" -> {
                        t = new Rose();
                        ((Rose) t).setVariant(Rose.Variant.PINK);
                    }
                    case "rose:black" -> {
                        t = new Rose();
                        ((Rose) t).setVariant(Rose.Variant.BLACK);
                    }

                    case "vending_machine" -> t = new VendingMachine(new AutoBrick(3), 3);

                    case "pumpkin" -> e = new Pumpkin();
                    case "spawn" -> {
                        e = den = new Den();
                        //add two baby foxes next to the den
                        BabyFox fox1 = new BabyFox();
                        fox1.getTransform().setPos(transform.getPos().add(1, 0, 0, new Vector3f()));
                        addEntity(fox1);
                        BabyFox fox2 = new BabyFox();
                        fox2.getTransform().setPos(transform.getPos().add(-1, 0, 0, new Vector3f()));
                        addEntity(fox2);
                    }
                    case "trap" -> e = new BearTrap();
                    case "bush" -> e = new Bush();
                    case "shroom" -> e = new ItemEntity(UUID.randomUUID(), new Shroom());
                    case "brick" -> e = new ItemEntity(UUID.randomUUID(), new BrickItem(1));

                    case "dog_spawner" -> {
                        e = new Spawner<>(UUID.randomUUID(), 0, 15*20, Dog::new);
                        ((Spawner<?>) e).setRenderCooldown(false);
                    }
                    default -> {
                        return;
                    }
                }
                if (t != null) {
                    t.getTransform().set(transform);
                    addTerrain(t);
                }
                if (e != null) {
                    e.getTransform().set(transform);
                    addEntity(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setPos(den.getTransform().getPos());
    }

    public void gameover() {
        this.hud.setFade(true, 20, 0xFFFFFF);
        new Await(20, () -> client.setScreen(deathScreen.get()));
    }

    public void updateRain() {
        this.downpour = Math.random() < 0.5f ? 0f : ((float) Math.random() * 0.2f + 0.1f);
    }

    public void skipNight() {
        long time = getTime();
        int dayLen = getDayLength();
        int h = dayLen / 24;
        long timeToAdd = dayLen + (h / 2) - (time % dayLen);
        this.setTime(time + timeToAdd);

        for (Entity entity : entities.values()) {
            if (entity instanceof Bush bush)
                bush.setFruits(true);
            else if (entity instanceof Dog dog)
                dog.kill();
        }
    }

    public Entity getLookingEnemy() {
        //find from all enemies the closest one to the player that is in a 60 degree cone in front of the player and within 15 units distance
        Entity closest = null;
        float closestDist = 15f;
        for (Entity entity : entities.values()) {
            if (entity instanceof Dog dog && !dog.isDead()) {
                Vector3f toEntity = entity.getTransform().getPos().sub(player.getTransform().getPos(), new Vector3f());
                float dist = toEntity.length();
                if (dist < closestDist) {
                    Vector3f lookDir = player.getLookDir();
                    float angle = Math.toDegrees(Math.acos(lookDir.normalize().dot(toEntity.normalize())));
                    if (angle < 30f) {
                        closest = entity;
                        closestDist = dist;
                    }
                }
            }
        }
        return closest;
    }

    @Override
    public void tick() {
        super.tick();

        if (isPaused())
            return;

        if (firstNight && isNight()) {
            firstNight = false;
            ((SmallHud) hud).addDialog(10*20, Text.translated("whispers.night"));
        }

        //rain
        if (--downpourTime <= 0) {
            downpourTime = 30 * 20; //30s
            updateRain();
        }

        Vector3f camPos = player.getTransform().getPos();
        for (int i = 0; i < downpour * 100; i++) {
            RainParticle rain = new RainParticle(60, 0xFF6699CC, (int) (downpour * 3f));
            rain.setPos(camPos.x + ((float) Math.random() * 2f - 1f) * 16f, camPos.y + 20f, camPos.z + ((float) Math.random() * 2f - 1f) * 16f);
            addParticle(rain);
        }
    }

    @Override
    protected void postWorldRender(MatrixStack matrices, float delta) {
        super.postWorldRender(matrices, delta);
        //PostProcess.apply(PostProcess.VINTAGE);
        if (((ThePlayer) player).isOnShrooms())
            PostProcess.apply(PostProcess.TRIPPY);
        else if (player.isDead())
            PostProcess.apply(PostProcess.GRAYSCALE);
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
    protected void setSkyColors() {
        //sunrise
        skyColors.addProperty(6*60, new SkyColors.SkyProperties(
                0xFF4400, 0x859090, 0x404060, 0xE57E4B, 0x7F7F7F,
                64f, 80f, 1f, 1f, 0.5f,
                0xFF4400, 3f, 0f
        ));
        //day start
        skyColors.addProperty(7*60, new SkyColors.SkyProperties(
                0xFFEEDD, 0x446FD0, 0xBBAA66, 0xBFD3DE, 0xB0D0FF,
                96f, 192f, 0f, 1f, 0.05f,
                0xFFEEDD, 5f, 1f
        ));
        //day end
        skyColors.addProperty(17*60, new SkyColors.SkyProperties(
                0xFFEEDD, 0x446FD0, 0xBBAA66, 0xBFD3DE, 0xB0D0FF,
                96f, 192f, 0f, 1f, 0.05f,
                0xFFEEDD, 5f, 1f
        ));
        //sunset
        skyColors.addProperty(18*60, new SkyColors.SkyProperties(
                0xFF4400, 0x8844DD, 0x404060, 0xFF72AD, 0x90507F,
                64f, 80f, 1f, 1f, 0.5f,
                0xFF4400, 3f, 0f
        ));
        //night start
        skyColors.addProperty(19*60, new SkyColors.SkyProperties(
                0x07070F, 0x0C0C18, 0x404060, 0x0C0C18, 0x0A0A14,
                64f, 80f, 0f, 1f, 5f,
                0x07070F, 0.1f, 0f
        ));
        //night end
        skyColors.addProperty(5*60, new SkyColors.SkyProperties(
                0x07070F, 0x0C0C18, 0x404060, 0x0C0C18, 0x0A0A14,
                64f, 80f, 0f, 1f, 5f,
                0x07070F, 0.1f, 0f
        ));
    }

    @Override
    public void renderWater(Camera camera, MatrixStack matrices, float delta) {
        //toon water
        Shader.activeShader.setFloat("waveAmplitude", 0.5f);
        Shader.activeShader.setFloat("waveFrequency", 0.0005f);
        Shader.activeShader.setColorRGBA("color", 0xDD00BBFF);
        Shader.activeShader.setVec2("roughMetal", 0.5f, 0f);
        Shader.activeShader.setVec2("waveDir1", 1f / 3f, 0.3f / 3f);
        Shader.activeShader.setVec2("waveDir2", -0.28f / 3f, 0.7f / 3f);

        super.renderWater(camera, matrices, delta);
    }

    @Override
    public void keyPress(int key, int scancode, int action, int mods) {
        super.keyPress(key, scancode, action, mods);

        if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_Z)
                player.stopUsing();
            return;
        }

        switch (key) {
            case GLFW.GLFW_KEY_A -> this.camYaw += 45f;
            case GLFW.GLFW_KEY_D -> this.camYaw -= 45f;
            case GLFW.GLFW_KEY_W -> this.camZoom = Math.max(this.camZoom - 1f, 3f);
            case GLFW.GLFW_KEY_S -> this.camZoom = Math.min(this.camZoom + 1f, 15f);

            case GLFW.GLFW_KEY_Z -> this.player.useAction();
            case GLFW.GLFW_KEY_X -> this.player.attackAction();
            case GLFW.GLFW_KEY_C -> this.player.dropItem(-1, true);

            case GLFW.GLFW_KEY_G -> spawnDebugWeapons();
            case GLFW.GLFW_KEY_K -> player.kill();
        }
    }

    @Override
    public void respawn(boolean init) {
        this.player = new ThePlayer(!init);
        this.player.getAbilities().set(Abilities.Ability.CAN_BUILD, false);
        this.addEntity(this.player);

        if (!init) {
            //fint a BabyFox
            BabyFox fox = null;
            for (Entity entity : entities.values()) {
                if (entity instanceof BabyFox babyFox) {
                    fox = babyFox;
                    break;
                }
            }
            if (fox != null) {
                this.player.getTransform().setPos(fox.getTransform().getPos());
                fox.remove();
            } else { //den fallback
                this.player.getTransform().setPos(den.getTransform().getPos());
            }
        }
    }
}
