package game.render;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import window.Window;
import world.chunk.LocalBlockCoord;

import java.nio.FloatBuffer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static game.render.Camera.Camera_Movement.*;

public class Camera {
    float deltaTime;
    float aspectRatio;
    float lastX, lastY;

    LocalBlockCoord LocalBlockCoord = new LocalBlockCoord((byte)0, (byte) 0, (byte) 0);

    private FloatBuffer matrixBufferView;
    private FloatBuffer matrixBufferProjection;

    private Vector3f cameraPos;
    private Vector3f cameraFront;

    private Matrix4f view;
    private Matrix4f projection;

    private Vector3f cameraUp;
    private Vector3f cameraRight;

    boolean firstMouse = true;

    // Default camera values
    float YAW         = -90.0f;
    float PITCH       =  0.0f;
    float SPEED       =  2.5f;
    float SENSITIVITY =  0.1f;
    float ZoomSENSITIVITY =  2.0f;
    float FOV         =  45.0f;

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
        matrixBufferProjection = BufferUtils.createFloatBuffer(16);
        projection.get(matrixBufferProjection);

        cameraFront = new Vector3f(0.0f, 0.0f, -2.0f);
        cameraPos = new Vector3f(0.0f, 0.0f, 10.0f);
        cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
    }

    public void update(Window window) {
        processKeyboard(window);
        processMouse(window);
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

        if (mouvement == FORWARD)
            movement.add(new Vector3f(cameraFront).mul(velocity));
        if (mouvement == BACKWARD)
            movement.sub(new Vector3f(cameraFront).mul(velocity));
        if (mouvement == LEFT)
            movement.sub(new Vector3f(cameraRight).mul(velocity));
        if (mouvement == RIGHT)
            movement.add(new Vector3f(cameraRight).mul(velocity));
        if (mouvement == UP)
            movement.add(new Vector3f(cameraUp).mul(velocity));
        if (mouvement == DOWN)
            movement.sub(new Vector3f(cameraUp).mul(velocity));

        cameraPos.add(movement);
    }

    private void mouse_callback(float xpos, float ypos) {
        if (firstMouse)
        {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        float xoffset = xpos - lastX;
        float yoffset = lastY - ypos;
        lastX = xpos;
        lastY = ypos;

        xoffset *= SENSITIVITY;
        yoffset *= SENSITIVITY;

        YAW   += xoffset;
        PITCH += yoffset;

        if(PITCH > 89.0f)
            PITCH = 89.0f;
        if(PITCH < -89.0f)
            PITCH = -89.0f;

        Vector3f direction = new Vector3f();
        direction.x = (float) (cos(Math.toRadians(YAW)) * cos(Math.toRadians(PITCH)));
        direction.y = (float) sin(Math.toRadians(PITCH));
        direction.z = (float) (sin(Math.toRadians(YAW)) * cos(Math.toRadians(PITCH)));
        cameraFront = direction.normalize();
        cameraRight = new Vector3f(cameraFront).cross(cameraUp).normalize();
    }

    private void processMouse(@NotNull Window window) {
        glfwSetCursorPosCallback(window.getWindowID(), (win,  xPos, yPos) -> mouse_callback((float)xPos, (float)yPos));
        glfwSetScrollCallback(window.getWindowID(), (win, xOffset, yOffset) -> {
            FOV -= (float)yOffset * ZoomSENSITIVITY;
            if (FOV < 1.0f)
                FOV = 1.0f;
            if (FOV > 45.0f)
                FOV = 45.0f;

            projection.identity().perspective((float)Math.toRadians(FOV), aspectRatio, 0.1f, 100.0f);
            projection.get(matrixBufferProjection);
        });
    }

    /**
     * Crée la matrice de projection isométrique
     */
    private Matrix4f createIsometricProjectionMatrix() {
        Matrix4f matrix = new Matrix4f();

        // Projection orthogonale
        matrix.ortho(-10.0f, 10.0f, -10.0f, 10.0f, -100.0f, 100.0f);

        // Vue isométrique avec angle correct pour préserver les proportions
        matrix.rotateX((float) Math.toRadians(-35.264f));  // Angle mathématiquement correct
        matrix.rotateY((float) Math.toRadians(45));        // Rotation de 45°

        // Échelle pour ajuster la taille à l'écran
        matrix.scale(1.5f, 2.3f, 1.5f);

        return matrix;
    }

    public void updateViewMatrix() {
        view.identity();
        view.lookAt(
                cameraPos,
                new Vector3f(LocalBlockCoord.x(), LocalBlockCoord.y(), LocalBlockCoord.z()).add(cameraFront),
                cameraUp
        );
        matrixBufferView.clear();
        view.get(matrixBufferView);
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public void setDeltaTime(float deltatime) {
        this.deltaTime = deltatime;
    }
}