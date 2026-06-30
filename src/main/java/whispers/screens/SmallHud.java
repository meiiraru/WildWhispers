package whispers.screens;

import cinnamon.Client;
import cinnamon.gui.widgets.types.ProgressBar;
import cinnamon.math.collision.Hit;
import cinnamon.model.GeometryHelper;
import cinnamon.model.Vertex;
import cinnamon.render.Font;
import cinnamon.render.MatrixStack;
import cinnamon.render.Window;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.render.texture.Texture;
import cinnamon.text.Style;
import cinnamon.text.Text;
import cinnamon.utils.*;
import cinnamon.world.Hud;
import cinnamon.world.WorldObject;
import cinnamon.world.entity.Entity;
import cinnamon.world.terrain.Terrain;
import whispers.entities.Pumpkin;
import whispers.entities.ThePlayer;
import whispers.terrain.VendingMachine;
import whispers.world.TestWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SmallHud extends Hud {

    public static final Resource
            icons = new Resource("whispers", "textures/icons.png"),
            pips  = new Resource("whispers", "textures/food.png");

    private int dialogDuration, dialogTicks;
    private Text dialogText;
    private int controlsTicks;

    private float lastHPquery = 1f;
    private int lastHPShow = 0;

    private float lastStaminaQuery = 1f;
    private int lastStaminaShow = 0;

    private final Set<Hints> shownHints = new HashSet<>();

    private ProgressBar stamina;

    @Override
    public void init() {
        health = new ProgressBar(0, 0, 60, 8, 1f);
        health.setColor(Colors.RED);

        stamina = new ProgressBar(0, 0, 60, 8, 1f);
        stamina.setColor(Colors.YELLOW);
    }

    @Override
    public void tick() {
        super.tick();

        if (Client.getInstance().world.isPaused())
            return;

        if (dialogTicks < dialogDuration)
            dialogTicks++;

        for (Hints shownHint : shownHints) {
            if (shownHint.hint.isDone())
                continue;
            shownHint.hint.tick();
        }

        if (lastHPShow > 0)
            lastHPShow--;

        if (lastStaminaShow > 0)
            lastStaminaShow--;

        if (controlsTicks > 0)
            controlsTicks--;
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        ThePlayer player = (ThePlayer) Client.getInstance().world.player;

        int width = Client.getInstance().window.scaledWidth;
        int height = Client.getInstance().window.scaledHeight;

        Pair<Hit, ? extends WorldObject> hit = player.getLookingObject(player.getPickRange());
        boolean vendingMachine = hit != null && hit.second() instanceof VendingMachine vm && player.getFood() >= vm.getCost();
        boolean pumpkin = hit != null && hit.second() instanceof Pumpkin pmk && pmk.canBeEaten();

        if (vendingMachine || pumpkin) {
            float x = (width - 23) / 2f;
            float y = height * 0.66f;

            Text press = Text.translated("whispers.buy_press_1").append(" ").withStyle(Style.EMPTY.outlined(true));
            Text toBuy = Text.of(" ").append(Text.translated(vendingMachine ? "whispers.buy_press_2" : "whispers.eat_press_2").withStyle(Style.EMPTY.outlined(true)));

            press.render(VertexConsumer.MAIN, matrices, x, y + 9, Alignment.CENTER_RIGHT);
            toBuy.render(VertexConsumer.MAIN, matrices, x + 23, y + 9, Alignment.CENTER_LEFT);

            //23x18
            //render buy button
            VertexConsumer.MAIN.consume(
                    GeometryHelper.quad(matrices, x, y, 23, 18),
                    new Resource("whispers", "textures/buy.png")
            );
        }

        float hp = player.getHealthProgress();

        if (hp != lastHPquery) {
            lastHPquery = hp;
            lastHPShow = 100;
        }
        health.setProgress(hp);
        health.setPos(4 + 16 + 2, height / 2 + 2 + health.getHeight() + 4);

        if (lastHPShow > 0) {
            health.render(matrices, 0, 0, delta);
            VertexConsumer.MAIN.consume(
                    GeometryHelper.quad(matrices, 4, height / 2f + 2 + 8, 16, 16),
                    new Resource("whispers", "textures/icons/heart.png")
            );
        }

        float stami = player.getStamina() / 100f;
        if (stami != lastStaminaQuery) {
            lastStaminaQuery = stami;
            lastStaminaShow = 100;
        }
        this.stamina.setProgress(stami);
        this.stamina.setPos(4 + 16 + 2, health.getY() + health.getHeight() + 4 + 2 + 4);

        if (lastStaminaShow > 0) {
            stamina.render(matrices, 0, 0, delta);
            VertexConsumer.MAIN.consume(
                    GeometryHelper.quad(matrices, 4, health.getY() + health.getHeight() + 4 + 2, 16, 16),
                    new Resource("whispers", "textures/icons/energy.png")
            );
        }

        int food = player.getFood();
        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, stamina.getY() + stamina.getHeight() + 4 + 2, 16, 16),
                new Resource("whispers", "textures/icons/food.png")
        );
        Text.of("x" + food)
                .withStyle(Style.EMPTY.outlined(true))
                .render(VertexConsumer.MAIN, matrices, 4 + 16 + 2, stamina.getY() + stamina.getHeight() + 4 + 2 + 8, Alignment.CENTER_LEFT);

        int lives = ((TestWorld) player.getWorld()).lives;
        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, stamina.getY() + stamina.getHeight() + 4 + 2 + 16 + 2, 16, 16),
                new Resource("whispers", "textures/icons/fox.png")
        );
        Text.of("x" + lives)
                .withStyle(Style.EMPTY.outlined(true))
                .render(VertexConsumer.MAIN, matrices, 4 + 16 + 2, stamina.getY() + stamina.getHeight() + 4 + 2 + 16 + 2 + 8, Alignment.CENTER_LEFT);

        int day = player.getWorld().getDay();
        VertexConsumer.MAIN.consume(
                GeometryHelper.quad(matrices, 4, stamina.getY() + stamina.getHeight() + 4 + 2 + 16 + 2 + 16 + 2, 16, 16),
                new Resource("whispers", "textures/icons/day.png")
        );
        Text.translated("whispers.day_count", day + 1)
                .withStyle(Style.EMPTY.outlined(true))
                .render(VertexConsumer.MAIN, matrices, 4 + 16 + 2, stamina.getY() + stamina.getHeight() + 4 + 2 + 16 + 2 + 16 + 2 + 8, Alignment.CENTER_LEFT);

        //render score
        //Text.of("Score: " + ((TestWorld) player.getWorld()).score)
        //        .render(VertexConsumer.MAIN, matrices, width / 2f, 4, Alignment.TOP_CENTER);

        //render dialog
        renderDialog(matrices, delta);

        //render hints
        for (Hints shownHint : shownHints) {
            if (shownHint.hint.isDone())
                continue;

            if (shownHint.hint.isActive()) {
                shownHint.hint.render(matrices, delta);
            }
        }

        //render controls
        if (controlsTicks > 0) {
            int fade = 20;
            float alpha = Math.min((float) controlsTicks / fade, 1f);
            int color = ((int) (alpha * 0xFF) << 24) | 0xFFFFFF;

            Vertex[] vertices = GeometryHelper.quad(matrices, 4, 4, 116, 116);
            for (Vertex vertex : vertices)
                vertex.color(color);

            VertexConsumer.MAIN.consume(vertices, new Resource("whispers", "textures/hints/controls.png"));
        }

        //draw vignette
        drawVignette(matrices, player, delta);

        //draw fade
        drawFade(matrices, Client.getInstance(), delta);

        VertexConsumer.finishAllBatches(Client.getInstance().camera);
    }

    protected void renderDialog(MatrixStack matrices, float delta) {
        if (dialogTicks >= dialogDuration || dialogText == null)
            return;

        float deltaTicks = dialogTicks + delta;
        float fadeTicks = 20;
        float alpha;

        //fade in
        if (deltaTicks < fadeTicks)
            alpha = Math.min(deltaTicks / fadeTicks, 1f);
        //fade out
        else if (deltaTicks > dialogDuration - fadeTicks)
            alpha = Math.min((dialogDuration - deltaTicks) / fadeTicks, 1f);
        else
            alpha = 1f;

        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        Window window = Client.getInstance().window;

        //text
        List<Text> texts = TextUtils.warpToWidth(dialogText, window.scaledWidth - 8);
        Style s = Style.EMPTY.color(color);
        Font f = dialogText.getStyle().getGuiSkin().getFont();
        float textHeight = TextUtils.getHeight(texts);
        float line = f.lineHeight + f.lineGap;

        //background
        UIHelper.nineQuad(VertexConsumer.MAIN, matrices, new Resource("whispers", "textures/dialog_box.png"),
                0, window.scaledHeight - textHeight - 8, window.scaledWidth, textHeight + 8, 0f, 0f, 16, 16, 16, 16, color
        );

        for (int i = 0; i < texts.size(); i++)
            texts.get(i).withStyle(s).render(VertexConsumer.MAIN, matrices, 4, window.scaledHeight - 4 - textHeight + i * line);
    }

    public void displayControls(int duration) {
        this.controlsTicks = duration;
    }

    public void addDialog(int duration, Text text) {
        this.dialogDuration = duration;
        this.dialogTicks = 0;
        this.dialogText = text;
    }

    public void displayHint(Hints hint) {
        if (shownHints.contains(hint))
            return;

        shownHints.add(hint);
        hint.hint.reset();
    }

    public enum Hints {
        MOVE(0, Text.translated("whispers.use", "\u2191\u2190\u2193\u2192"), Text.translated("whispers.to_move"), new Resource("whispers", "textures/hints/move.png")),
        JUMP(1, Text.translated("whispers.press", "SPACE"), Text.translated("whispers.to_jump"), new Resource("whispers", "textures/hints/jump.png")),
        CAMERA(1, Text.translated("whispers.use", "WASD"), Text.translated("whispers.to_look"), new Resource("whispers", "textures/hints/camera.png")),
        SPRINT(0, Text.translated("whispers.press", "SHIFT"), Text.translated("whispers.to_sprint"), new Resource("whispers", "textures/hints/sprint.png")),
        INTERACT(2, Text.translated("whispers.press", "Z"), Text.translated("whispers.to_use"), new Resource("whispers", "textures/hints/interact.png")),
        DROP(3, Text.translated("whispers.press", "C"), Text.translated("whispers.to_drop"), new Resource("whispers", "textures/hints/drop.png"));

        private final Hint hint;

        Hints(int index, Text text1, Text text2, Resource image) {
            this.hint = new Hint(text1, text2, image, index);
        }
    }

    private static final class Hint {
        private final Text text1, text2;
        private final Resource image;
        private final int width, height;
        private final int index;

        private static final int displayTime = 10 * 20;
        private int ticks = -1;

        private Hint(Text text1, Text text2, Resource image, int index) {
            this.text1 = text1;
            this.text2 = text2;
            this.image = image;

            Texture texture = Texture.of(image);
            this.width = texture.getWidth();
            this.height = texture.getHeight();

            this.index = index;
        }

        public void tick() {
            ticks++;
        }

        public boolean isActive() {
            return ticks >= 0;
        }

        public boolean isDone() {
            return ticks >= displayTime;
        }

        public void reset() {
            this.ticks = 0;
        }

        public void render(MatrixStack matrices, float delta) {
            float deltaTicks = ticks + delta;
            float fade = 20;
            float alpha;

            //fade in
            if (deltaTicks < fade)
                alpha = Math.min(deltaTicks / fade, 1f);
            //fade out
            else if (deltaTicks > displayTime - fade)
                alpha = Math.min((displayTime - deltaTicks) / fade, 1f);
            else
                alpha = 1f;

            int color = (int) (alpha * 255) << 24 | 0xFFFFFF;

            float x = width / 2f;

            matrices.pushMatrix();
            matrices.translate(Client.getInstance().window.scaledWidth - width, index * 50, 0);

            //bg
            Vertex[] vertices = GeometryHelper.quad(matrices, 0, 0, width, height);
            for (Vertex vertex : vertices)
                vertex.color(color);

            VertexConsumer.MAIN.consume(vertices, image);

            //text
            Style s = Style.EMPTY.color(color);
            Text.empty().withStyle(s).append(text1).render(VertexConsumer.MAIN, matrices, x, 4, Alignment.TOP_CENTER);
            Text.empty().withStyle(s).append(text2).render(VertexConsumer.MAIN, matrices, x, height - 4, Alignment.BOTTOM_CENTER);

            matrices.popMatrix();
        }

    }
}
