package game.render;

import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class Camera {
    float deltaTime;

    private Matrix4f projection;
    private FloatBuffer matrixBufferProjection;

    public void init() {
        projection = createIsometricProjectionMatrix();
        matrixBufferProjection = org.lwjgl.BufferUtils.createFloatBuffer(16);
        projection.get(matrixBufferProjection);
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

    public Matrix4f getProjection() {
        return projection;
    }

    public void setDeltaTime(float deltatime) {
        this.deltaTime = deltatime;
    }
}