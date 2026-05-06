package whispers.io;

import cinnamon.input.InputManager;
import cinnamon.input.Movement;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.living.Player;
import org.lwjgl.glfw.GLFW;
import whispers.entities.ThePlayer;

public class LocalInput extends Movement {

    @Override
    public void tick(Entity target) {
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_LEFT))  movement.x -= 1;
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) movement.x += 1;
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_UP))    movement.z += 1;
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_DOWN))  movement.z -= 1;

        if (target instanceof ThePlayer tp && tp.isOnShrooms()) {
            movement.x = -movement.x;
            movement.z = -movement.z;
        }

        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_SPACE)) movement.y += 1;

        if (target instanceof Player p) {
            boolean sprint = InputManager.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT);
            p.updateMovementFlags(false, sprint, false);
        }

        if (movement.lengthSquared() > 0) {
            applyImpulse(target, movement.x, movement.y, movement.z);
            movement.set(0);
        }
    }

    @Override
    protected void applyRotation(Entity target, float pitch, float yaw, float roll) {
        //super.applyRotation(target, pitch, yaw, roll);
    }
}
