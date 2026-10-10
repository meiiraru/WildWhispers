package whispers.world;

import cinnamon.events.Await;
import cinnamon.input.Keybind;
import cinnamon.model.material.Material;
import cinnamon.model.material.MaterialTexture;
import cinnamon.parsers.BBModelTerrainLoader;
import cinnamon.registry.TerrainRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.WaterRenderer;
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
import cinnamon.world.entity.projectile.Brick;
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
import whispers.items.AutoBrick;
import whispers.items.Shroom;
import whispers.screens.PauseMenu;
import whispers.screens.SmallHud;
import whispers.screens.WhisperDeath;
import whispers.terrain.Tree;
import whispers.terrain.VendingMachine;

import java.util.UUID;

public class TestWorld extends WorldClient {

    public static final Keybind
            NONE_KEYBIND   = new Keybind("whispers.key.none",   GLFW.GLFW_KEY_UNKNOWN,    Keybind.KeyType.KEY),
            LEFT_KEYBIND   = new Keybind("whispers.key.left",   GLFW.GLFW_KEY_LEFT,       Keybind.KeyType.KEY),
            RIGHT_KEYBIND  = new Keybind("whispers.key.right",  GLFW.GLFW_KEY_RIGHT,      Keybind.KeyType.KEY),
            UP_KEYBIND     = new Keybind("whispers.key.up",     GLFW.GLFW_KEY_UP,         Keybind.KeyType.KEY),
            DOWN_KEYBIND   = new Keybind("whispers.key.down",   GLFW.GLFW_KEY_DOWN,       Keybind.KeyType.KEY),
            JUMP_KEYBIND   = new Keybind("whispers.key.jump",   GLFW.GLFW_KEY_SPACE,      Keybind.KeyType.KEY),
            SPRINT_KEYBIND = new Keybind("whispers.key.sprint", GLFW.GLFW_KEY_LEFT_SHIFT, Keybind.KeyType.KEY),
            USE_KEYBIND    = new Keybind("whispers.key.use",    GLFW.GLFW_KEY_Z,          Keybind.KeyType.KEY),
            ATTACK_KEYBIND = new Keybind("whispers.key.attack", GLFW.GLFW_KEY_X,          Keybind.KeyType.KEY),
            DROP_KEYBIND   = new Keybind("whispers.key.drop",   GLFW.GLFW_KEY_C,          Keybind.KeyType.KEY);

    public float camYaw = 135f, camYawLerp = 135f, camZoom = 7.5f, camZoomLerp = 10f;

    private boolean firstNight = true;
    private float downpour = 0f;
    private int downpourTime = 0;

    private Den den;

    public int score = 0;
    public int lives = 1;

    public TestWorld() {
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
                .volume(0.3f)
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

                    case "pumpkin" -> e = new Spawner<>(UUID.randomUUID(), 40*20, () -> new Pumpkin(1000 / ((int) (Math.random() * 3f) + 1)));
                    case "spawn" -> {
                        e = den = new Den();
                        //add two baby foxes next to the den
                        BabyFox fox1 = new BabyFox();
                        fox1.getTransform().setPos(transform.getPos().add(1, 0, 0, new Vector3f()));
                        addEntity(fox1);
                        BabyFox fox2 = new BabyFox();
                        fox2.getTransform().setPos(transform.getPos().add(-1, 0, 0, new Vector3f()));
                        addEntity(fox2);
                        lives += 2;
                    }
                    case "trap" -> e = new BearTrap();
                    case "bush" -> e = new Bush();
                    case "shroom" -> e = new ItemEntity(UUID.randomUUID(), new Shroom());
                    case "brick" -> e = new ItemEntity(UUID.randomUUID(), new BrickItem(1));

                    case "dog_spawner" -> {
                        e = new Spawner<>(UUID.randomUUID(), 15*20, Dog::new);
                        ((Spawner<?>) e).setRenderCooldown(false);
                    }
                    case "bird_spawner" -> {
                        Vector3f pos = transform.getPos();
                        float x = pos.x, y = pos.y, z = pos.z;
                        e = new Spawner<>(UUID.randomUUID(), 30*20, () -> new Bird(x, y, z));
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

        playerEntity.setPos(den.getTransform().getPos());
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
            if ((entity instanceof Dog dog && !dog.isDead()) || (entity instanceof Bird bird && !bird.isDead())) {
                Vector3f toEntity = entity.getTransform().getPos().sub(playerEntity.getTransform().getPos(), new Vector3f());
                float dist = toEntity.length();
                if (dist < closestDist) {
                    Vector3f lookDir = playerEntity.getLookDir();
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

        Vector3f camPos = playerEntity.getTransform().getPos();
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
        if (((ThePlayer) playerEntity).isOnShrooms())
            PostProcess.apply(PostProcess.TRIPPY);
        else if (((ThePlayer) playerEntity).isDead())
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
        skyColors.addProperty(6*60, new SkyColors.SkyProperties()
                .sunColor(0xFF4400).skyColor(0x859090).ambientLight(0x404060).fogColor(0xE57E4B).cloudsColor(0x7F7F7F)
                .fogStart(64f).fogEnd(80f).sunIntensity(1f).fogIntensity(1f).starsIntensity(0.5f)
                .sunlightColor(0xFF4400).sunlightIntensity(3f).sunlightShadowIntensity(0f)
        );
        //day start
        skyColors.addProperty(7*60, new SkyColors.SkyProperties()
                .sunColor(0xFFEEDD).skyColor(0x446FD0).ambientLight(0xBBAA66).fogColor(0xBFD3DE).cloudsColor(0xB0D0FF)
                .fogStart(96f).fogEnd(192f).sunIntensity(0f).fogIntensity(1f).starsIntensity(0.05f)
                .sunlightColor(0xFFEEDD).sunlightIntensity(5f).sunlightShadowIntensity(1f)
        );
        //day end
        skyColors.addProperty(17*60, new SkyColors.SkyProperties()
                .sunColor(0xFFEEDD).skyColor(0x446FD0).ambientLight(0xBBAA66).fogColor(0xBFD3DE).cloudsColor(0xB0D0FF)
                .fogStart(96f).fogEnd(192f).sunIntensity(0f).fogIntensity(1f).starsIntensity(0.05f)
                .sunlightColor(0xFFEEDD).sunlightIntensity(5f).sunlightShadowIntensity(1f)
        );
        //sunset
        skyColors.addProperty(18*60, new SkyColors.SkyProperties()
                .sunColor(0xFF4400).skyColor(0x8844DD).ambientLight(0x404060).fogColor(0xFF72AD).cloudsColor(0x90507F)
                .fogStart(64f).fogEnd(80f).sunIntensity(1f).fogIntensity(1f).starsIntensity(0.5f)
                .sunlightColor(0xFF4400).sunlightIntensity(3f).sunlightShadowIntensity(0f)
        );
        //night start
        skyColors.addProperty(19*60, new SkyColors.SkyProperties()
                .sunColor(0x07070F).skyColor(0x0C0C18).ambientLight(0x404060).fogColor(0x0C0C18).cloudsColor(0x0A0A14)
                .fogStart(64f).fogEnd(80f).sunIntensity(0f).fogIntensity(1f).starsIntensity(5f)
                .sunlightColor(0x07070F).sunlightIntensity(0.1f).sunlightShadowIntensity(0f)
        );
        //night end
        skyColors.addProperty(5*60, new SkyColors.SkyProperties()
                .sunColor(0x07070F).skyColor(0x0C0C18).ambientLight(0x404060).fogColor(0x0C0C18).cloudsColor(0x0A0A14)
                .fogStart(64f).fogEnd(80f).sunIntensity(0f).fogIntensity(1f).starsIntensity(5f)
                .sunlightColor(0x07070F).sunlightIntensity(0.1f).sunlightShadowIntensity(0f)
        );
    }

    @Override
    public void renderExtras(Camera camera, MatrixStack matrices, float delta) {
        super.renderExtras(camera, matrices, delta);

        //toon water
        WaterRenderer.bindRenderer();

        Shader.activeShader.setFloat("waveAmplitude", 0.5f);
        Shader.activeShader.setFloat("waveFrequency", 0.0005f);
        Shader.activeShader.setColorRGBA("color", 0xDD00BBFF);
        Shader.activeShader.setVec2("roughMetal", 0.5f, 0f);
        Shader.activeShader.setVec2("waveDir1", 1f / 3f, 0.3f / 3f);
        Shader.activeShader.setVec2("waveDir2", -0.28f / 3f, 0.7f / 3f);

        WaterRenderer.renderWaterPlane(camera, matrices, 0.9f, getSky().fogEnd);
    }

    @Override
    public void keyPress(int key, int scancode, int action, int mods) {
        super.keyPress(key, scancode, action, mods);

        if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_Z)
                ((ThePlayer) playerEntity).stopUsing();
            return;
        }

        switch (key) {
            case GLFW.GLFW_KEY_A -> this.camYaw += 45f;
            case GLFW.GLFW_KEY_D -> this.camYaw -= 45f;
            case GLFW.GLFW_KEY_W -> this.camZoom = Math.max(this.camZoom - 1f, 3f);
            case GLFW.GLFW_KEY_S -> this.camZoom = Math.min(this.camZoom + 1f, 15f);

            //case GLFW.GLFW_KEY_G -> spawnDebugWeapons();
            case GLFW.GLFW_KEY_K -> ((ThePlayer) playerEntity).kill();
            case GLFW.GLFW_KEY_H -> ((ThePlayer) playerEntity).giveItem(new AutoBrick(300));
            case GLFW.GLFW_KEY_J -> {
                for (Entity entity : entities.values()) {
                    if (entity instanceof Dog || entity instanceof Bird) {
                        Brick b = new Brick(UUID.randomUUID(), null);
                        b.setPos(entity.getTransform().getPos().add(0, 3, 0, new Vector3f()));
                        addEntity(b);
                    }
                }
            }
        }
    }

    @Override
    public void respawn(boolean init) {
        ThePlayer player = new ThePlayer(!init);
        player.getAbilities().set(Abilities.Ability.CAN_BUILD, false);
        this.playerEntity = player;
        this.addEntity(player);

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
                player.getTransform().setPos(fox.getTransform().getPos());
                fox.remove();
            } else { //den fallback
                player.getTransform().setPos(den.getTransform().getPos());
            }
        }
    }
}
