package game.render.renderers;

import game.render.Camera;
import game.render.loader.Shader;
import game.render.loader.Texture;
import org.joml.Matrix4f;
import systeme.exception.ShaderCompilationException;
import world.WorldManager;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class WorldRender implements GameRenderable {
    private int VAO, VBO, EBO;
    private final Shader shader;
    private final Texture texture;
    private final WorldManager world;

    private final float FOV = 0.6f; // Ajustez cette valeur pour zoomer/dézoomer

    // Vertices d'un cube 3D (position + coordonnées de texture)
    private final float[] cubeVertices = {
            // Face du DESSUS (Y=1)  vue d'en haut
            0.0f, 1.0f, 0.0f,         0.0f, 1.0f,  // coin arrière-gauche
            1.0f, 1.0f, 0.0f,         1.0f, 1.0f,  // coin arrière-droite
            1.0f, 1.0f, 1.0f,         1.0f, 0.0f,  // coin avant-droite
            0.0f, 1.0f, 1.0f,         0.0f, 0.0f,  // coin avant-gauche

            // Face DROITE (X=1)      côté droit du cube
            1.0f, 0.0f, 0.0f,         0.0f, 0.0f,  // arrière bas
            1.0f, 0.0f, 1.0f,         1.0f, 0.0f,  // avant-bas
            1.0f, 1.0f, 1.0f,         1.0f, 1.0f,  // avant-haut
            1.0f, 1.0f, 0.0f,         0.0f, 1.0f,  // arrière-haut

            // Face gauche (Z=0) - face du fond
            0.0f, 0.0f, 0.0f,         1.0f, 0.0f,  // gauche bas
            0.0f, 1.0f, 0.0f,         1.0f, 1.0f,  // gauche haut
            1.0f, 1.0f, 0.0f,         0.0f, 1.0f,  // droite haut
            1.0f, 0.0f, 0.0f,         0.0f, 0.0f   // droite basse
    };

    // Indices pour les 3 faces visibles en isométrique
    private final int[] indices = {
            // Face du dessus
            0, 2, 1,   0, 3, 2,
            // Face droite
            4, 6, 5,   4, 7, 6,
            // Face arrière
            8, 10, 9,  8, 11, 10
    };

    public WorldRender() {
        try {
            shader = new Shader("world");
        } catch (ShaderCompilationException e) {
            throw new RuntimeException(e);
        }
        try {
            texture = new Texture("grass");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        world = new WorldManager(); // Votre chunk avec un bloc à (0, 0, 0)
    }

    @Override
    public void initialize() {
        setupBuffers();
    }

    private void setupBuffers() {
        // Génération des buffers
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        glBindVertexArray(VAO);

        // Buffer des vertices
        glBindBuffer(GL_ARRAY_BUFFER, VBO);

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(cubeVertices.length);
        vertexBuffer.put(cubeVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Buffer des indices
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Attribut 0: Position (x, y, z)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Attribut 1 : Coordonnées texture (u, v)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    @Override
    public void render(Camera camera, float deltaTime) {
        shader.use();
        glBindVertexArray(VAO);

        // Activer la texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
        shader.getUniforms().setInt("u_texture", 0);

        // Créer et envoyer la matrice de projection isométrique
        Matrix4f projectionMatrix = createIsometricProjectionMatrix();
        setMatrix4f(shader, "u_projectionMatrix", projectionMatrix);

        // Pour chaque bloc dans le chunk
        world.forEach((chunkCoord, chunk) -> {
            chunk.forEach((coord, meshCube) -> {

                // Matrice de transformation pour positionner le bloc
                Matrix4f modelMatrix = new Matrix4f();
                modelMatrix.translate(coord.x(), coord.y(), coord.z());
                modelMatrix.translate(chunkCoord.x(), chunkCoord.y(), chunkCoord.z());

                setMatrix4f(shader, "u_modelMatrix", modelMatrix);

                // Dessiner le cube
                glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
            });
        });

        glBindVertexArray(0);
        shader.stop();
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
        matrix.scale(0.25f * FOV, 0.45f * FOV, 0.25f * FOV);

        return matrix;
    }

    /**
     * Méthode utilitaire pour envoyer une matrice 4x4 au shader
     */
    private void setMatrix4f(Shader shader, String uniformName, Matrix4f matrix) {
        FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);

        int location = glGetUniformLocation(shader.programID, uniformName);
        if (location != -1) {
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    @Override
    public void cleanup() {
        shader.cleanUp();
        texture.cleanUp();
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
    }
}