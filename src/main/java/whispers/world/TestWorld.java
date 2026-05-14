package whispers.world;

import cinnamon.events.Await;
import cinnamon.model.material.Material;
import cinnamon.model.material.MaterialTexture;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.WorldRenderer;
import cinnamon.render.shader.PostProcess;
import cinnamon.render.texture.Texture;
import cinnamon.sound.SoundCategory;
import cinnamon.text.Text;
import cinnamon.utils.Resource;
import cinnamon.utils.UIHelper;
import cinnamon.world.Abilities;
import cinnamon.world.WorldObject;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.collectable.ItemEntity;
import cinnamon.world.items.Item;
import cinnamon.world.particle.RainParticle;
import cinnamon.world.sky.SkyColors;
import cinnamon.world.terrain.Barrier;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;
import cinnamon.world.worldgen.TerrainGenerator;
import org.joml.Math;
import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import whispers.entities.*;
import whispers.io.LocalInput;
import whispers.items.Shroom;
import whispers.screens.SmallHud;
import whispers.screens.WhisperDeath;
import whispers.terrain.Fence;
import whispers.terrain.Pillar;
import whispers.terrain.Tombstone;
import whispers.terrain.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestWorld extends WorldClient {

    public float camYaw = 135f, camYawLerp = 135f, camZoom = 7.5f, camZoomLerp = 10f;

    private final List<WorldObject> objects = new ArrayList<>();
    private final Random rand = new Random(3141592L);

    private boolean firstNight = true;
    private float downpour = 0f;

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
        this.hud.setFade(true, 0, 0x000000);
        scheduledTicks.add(() -> new Await(30, () -> {
            this.hud.setFade(false, 60, 0x000000);
            ((SmallHud) hud).addDialog(10*20, Text.translated("whispers.intro"));
            new Await(10*20, () -> ((SmallHud) hud).addDialog(10*20, Text.translated("whispers.intro2")));
            new Await(20*20, () -> {
                ((SmallHud) hud).displayHint(SmallHud.Hints.JUMP);
                ((SmallHud) hud).displayHint(SmallHud.Hints.MOVE);
            });
            new Await(30*20, () -> {
                ((SmallHud) hud).displayHint(SmallHud.Hints.SPRINT);
                ((SmallHud) hud).displayHint(SmallHud.Hints.CAMERA);
            });
        }));

        this.dayLength = 5*60*20;
        this.nightStart = dayLength / 2;

        playSound(new Resource("whispers", "sounds/wind-rustling-grass.ogg"), SoundCategory.AMBIENT, new Vector3f())
                .loop(true)
                .distance(64f);

        Material grass = new Material("grass");
        grass.setAlbedo(new MaterialTexture(new Resource("whispers", "textures/grass.png"), Texture.TextureParams.MIPMAP));

        int r = 32;
        TerrainGenerator.fill(this, -r, 0, -r, r, 0, r, grass);
        setTimeMinutes(6*60+30); //6:30AM

        //add den
        Den den = new Den();
        den.setPos(0.5f, 1.5f, 0.5f);
        addEntity(den);

        Wisp w = new Wisp(false);
        w.setPos(0.5f, 3.5f, 0.5f);
        addEntity(w);

        //tp to cemetery
        CemeteryTP tp = new CemeteryTP();
        tp.setPos(0.5f, 1f, -32f);
        addEntity(tp);
        tp.setEnterTrigger(e -> {
            if (isNight() && e instanceof ThePlayer pl && !pl.isOnTeleportCooldown()) {
                this.hud.setFade(true, 20, 0x000000);
                new Await(20, () -> {
                    pl.setTeleportCooldown(40);
                    pl.setPos(0.5f, 1.5f, 500.5f);
                    this.hud.setFade(false, 20, 0x000000);
                });
            }
        });

        Wisp w2 = new Wisp(false);
        w2.setPos(0.5f, 3.5f, -32f);
        addEntity(w2);

        genWorld();

        ((ThePlayer) player).setHunger(35);
    }

    public void genWorld() {
        for (WorldObject object : objects) {
           if (object instanceof Entity entity)
                entity.remove();
             else if (object instanceof Terrain terrain)
                removeTerrain(terrain);
        }

        int r = 32;
        int rr = r + 1;

        //add barriers
        for (int i = -rr; i <= rr; i++) {
            for (int j = -rr; j <= rr; j++) {
                if (i == -rr || i == rr || j == -rr || j == rr) {
                    Barrier barrier = new Barrier();
                    barrier.getCollisionMask().setMask(1, true);
                    barrier.setPos(i, 1.5f, j);
                    addTerrain(barrier);
                    objects.add(barrier);
                }
            }
        }

        //add trees
        for (int i = 0; i < 80; i++) {
            Tree tree = new Tree();
            tree.setPos((rand.nextFloat() * 2f - 1f) * r, rand.nextFloat() * 3f - 1.5f, (rand.nextFloat() * 2f - 1f) * r);
            tree.setRotation((byte) rand.nextInt(4));
            tree.getCollisionMask().setMask(1, true);
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
        for (int i = 0; i < 5; i++) {
            Pumpkin pumpkin = new Pumpkin();
            pumpkin.setPos((rand.nextFloat() * 2f - 1f) * r, 1.5f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(pumpkin);
            objects.add(pumpkin);
        }

        //shrooms
        for (int i = 0; i < 1; i++) {
            ItemEntity item = new ItemEntity(UUID.randomUUID(), new Shroom());
            item.setPos((rand.nextFloat() * 2f - 1f) * r, 3f, (rand.nextFloat() * 2f - 1f) * r);
            addEntity(item);
            objects.add(item);
        }

        //generate the cemetery
        genCemetery();
    }

    protected void genCemetery() {
        //generate a new cemetery 500 blocks away on Z
        int r = 16;
        int rr = r + 1;

        //floor
        Material dirt = new Material("dirt");
        dirt.setAlbedo(new MaterialTexture(new Resource("whispers", "textures/dirt.png"), Texture.TextureParams.MIPMAP));

        TerrainGenerator.fill(this, -r, 0, 500 - r, r, 0, 500 + r, dirt);

        //barrier
        for (int i = -rr; i <= rr; i++) {
            for (int j = 500 - rr; j <= 500 + rr; j++) {
                if (i == -rr || i == rr || j == 500 - rr || j == 500 + rr) {
                    Barrier barrier = new Barrier();
                    barrier.getCollisionMask().setMask(1, true);
                    barrier.setPos(i, 4.5f, j);
                    addTerrain(barrier);
                }
            }
        }

        //add tombstones
        float tr = r - 2f;
        for (int i = 0; i < 15; i++) {
            Tombstone tombstone = new Tombstone(rand.nextInt(2));
            tombstone.setPos((rand.nextFloat() * 2f - 1f) * tr, 1f, 500 + (rand.nextFloat() * 2f - 1f) * tr);
            tombstone.setRotation((byte) rand.nextInt(4));
            addTerrain(tombstone);
        }

        //add wisps
        for (int i = 0; i < 8; i++) {
            Wisp wisp = new Wisp(true);
            wisp.setPos((rand.nextFloat() * 2f - 1f) * tr, 3f, 500 + (rand.nextFloat() * 2f - 1f) * tr);
            addEntity(wisp);
        }

        //add pillars at borders every 4 blocks
        for (int i = -r; i <= r; i += 4) {
            Pillar pillar1 = new Pillar();
            pillar1.setPos(-r, 1f, 500 + i);
            addTerrain(pillar1);

            Pillar pillar2 = new Pillar();
            pillar2.setPos(r, 1f, 500 + i);
            addTerrain(pillar2);

            Pillar pillar3 = new Pillar();
            pillar3.setPos(i, 1f, 500 - r);
            addTerrain(pillar3);

            Pillar pillar4 = new Pillar();
            pillar4.setPos(i, 1f, 500 + r);
            addTerrain(pillar4);
        }

        //add fences between the pillars
        for (int i = -r; i < r; i++) {
            if (i % 4 == 0)
                continue;

            Fence fence1 = new Fence();
            fence1.setPos(-r, 1f, 500 + i);
            fence1.setRotation((byte) 1);
            addTerrain(fence1);

            Fence fence2 = new Fence();
            fence2.setPos(r, 1f, 500 + i);
            fence2.setRotation((byte) 1);
            addTerrain(fence2);

            Fence fence3 = new Fence();
            fence3.setPos(i, 1f, 500 - r);
            addTerrain(fence3);

            Fence fence4 = new Fence();
            fence4.setPos(i, 1f, 500 + r);
            addTerrain(fence4);
        }

        //add teleport back to main area
        MainWorldTP tp = new MainWorldTP();
        tp.setPos(0.5f, 1f, 500.5f - tr);
        addEntity(tp);
        tp.setEnterTrigger(e -> {
            if (e instanceof ThePlayer pl && !pl.isOnTeleportCooldown()) {
                this.hud.setFade(true, 20, 0x000000);
                new Await(20, () -> {
                    pl.setTeleportCooldown(40);
                    e.setPos(0.5f, 1.5f, 0.5f);
                    this.hud.setFade(false, 20, 0x000000);
                });
            }
        });
    }

    public int getRequiredFood() {
        return 10 + 5 * getDay();
    }

    public void gameover() {
        this.hud.setFade(true, 20, 0xFFFFFF);
        new Await(20, () -> client.setScreen(deathScreen.get()));
    }

    public void eep() {
        this.downpour = Math.random() < 0.5f ? 0f : ((float) Math.random() * 0.2f + 0.1f);
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

        Item held = player.getHoldingItem();
        if (held != null) {
            ((SmallHud) hud).displayHint(SmallHud.Hints.INTERACT);
            ((SmallHud) hud).displayHint(SmallHud.Hints.DROP);
        }

        //rain
        Vector3f camPos = player.getPos();
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
    public void keyPress(int key, int scancode, int action, int mods) {
        super.keyPress(key, scancode, action, mods);

        if (action == GLFW.GLFW_RELEASE)
            return;

        switch (key) {
            case GLFW.GLFW_KEY_A -> this.camYaw += 45f;
            case GLFW.GLFW_KEY_D -> this.camYaw -= 45f;
            case GLFW.GLFW_KEY_W -> this.camZoom = Math.max(this.camZoom - 1f, 3f);
            case GLFW.GLFW_KEY_S -> this.camZoom = Math.min(this.camZoom + 1f, 15f);

            case GLFW.GLFW_KEY_Z -> this.player.useAction();
            case GLFW.GLFW_KEY_X -> this.player.attackAction();
            case GLFW.GLFW_KEY_C -> this.player.dropItem(-1);
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
}
