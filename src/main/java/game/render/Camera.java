package game.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import window.Window;
import world.WorldCoord;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static game.render.Camera.Camera_Movement.*;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class Camera {
    float deltaTime;

    float aspectRatio;
    float lastX, lastY;

    private Matrix4f view;
    private FloatBuffer matrixBufferView;

    private Matrix4f projection;
    private FloatBuffer matrixBufferProjection;

    private Vector3f cameraUp;
    private Vector3f cameraRight;

    // Default camera values
    float YAW         = -90.0f;
    float PITCH       =  0.0f;
    float SPEED       =  2.5f;
    float SENSITIVITY =  0.1f;
    float ZoomSENSITIVITY =  2.0f;
    float FOV         =  45.0f;

    private WorldCoord cameraPos;
    private Vector3f cameraFront;

    enum Camera_Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public Camera(float deltaTime, float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.deltaTime = deltaTime;
    }

    public void init() {
        view = new Matrix4f();
        matrixBufferView = BufferUtils.createFloatBuffer(16);
        view.get(matrixBufferView);

        projection = createIsometricProjectionMatrix();
        matrixBufferProjection = org.lwjgl.BufferUtils.createFloatBuffer(16);
        projection.get(matrixBufferProjection);

        cameraFront = new Vector3f(0.0f, 0.0f, -2.0f);
        cameraPos = new WorldCoord(new Vector3f(0));
        cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
    }

    public void update(Window window) {
        processKeyboard(window);
        updateViewMatrix();
    }

    private void processKeyboard(Window window) {
        if(glfwGetKey(window.getWindowID(), GLFW_KEY_W) == GLFW_PRESS) applyMouvement(FORWARD);
        else if(glfwGetKey(window.getWindowID(), GLFW_KEY_S) == GLFW_PRESS) applyMouvement(BACKWARD);

        if(glfwGetKey(window.getWindowID(), GLFW_KEY_A) == GLFW_PRESS) applyMouvement(LEFT);
        else if(glfwGetKey(window.getWindowID(), GLFW_KEY_D) == GLFW_PRESS) applyMouvement(RIGHT);

        if(glfwGetKey(window.getWindowID(), GLFW_KEY_SPACE) == GLFW_PRESS) applyMouvement(UP);
        else if(glfwGetKey(window.getWindowID(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) applyMouvement(DOWN);
    }

    private void applyMouvement(Camera_Movement mouvement) {
        float velocity = SPEED * deltaTime;
        Vector3f movement = new Vector3f();

        // Vecteurs adaptés à la vue isométrique (angle 45° Y)
        Vector3f isoForward = new Vector3f(-1, 0, -1).normalize();  // vers le "haut" de l'écran
        Vector3f isoRight = new Vector3f(1, 0, -1).normalize();     // vers la "droite" de l'écran
        Vector3f isoUp = new Vector3f(0, 1, 0);                     // vers le haut

        switch (mouvement) {
            case FORWARD -> movement.add(new Vector3f(isoForward).mul(velocity));
            case BACKWARD -> movement.sub(new Vector3f(isoForward).mul(velocity));
            case LEFT -> movement.sub(new Vector3f(isoRight).mul(velocity));
            case RIGHT -> movement.add(new Vector3f(isoRight).mul(velocity));
            case UP -> movement.add(new Vector3f(isoUp).mul(velocity));
            case DOWN -> movement.sub(new Vector3f(isoUp).mul(velocity));
        }

        cameraPos.coord.add(movement);
    }

    /**
     * Crée la matrice de projection isométrique
     */
    private Matrix4f createIsometricProjectionMatrix() {
        Matrix4f matrix = new Matrix4f();

        // Projection orthogonale
        matrix.ortho(-10.0f, 10.0f, -10.0f, 10.0f, -100.0f, 100.0f);


        return matrix;
    }

    public void updateViewMatrix() {
        view.identity();

        // D'abord, on applique les rotations isométriques
        view.rotateX((float) Math.toRadians(-35.264f));
        view.rotateY((float) Math.toRadians(45));

        // Optionnel : échelle pour ajuster la taille
        view.scale(1.5f, 2.3f, 1.5f);

        view.translate(new Vector3f(cameraPos.coord).negate());

        matrixBufferView.clear();
        view.get(matrixBufferView);
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getView() {
        return view;
    }

    public void setDeltaTime(float deltatime) {
        this.deltaTime = deltatime;
    }
}