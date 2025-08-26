package game.render;

import org.joml.Vector3f;

public class Camera {
    Vector3f position = new Vector3f(0, 1, 5);

    public Vector3f getPosition() {
        return position;
    }
}