package game.render.renderers;

import game.render.Camera;
import game.render.GameRenderable;
import game.render.loader.Shader;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class MeshRender implements GameRenderable {
    private int VAO;
    private final Shader shader;

    public MeshRender() {
        try {
            // Utiliser le shader triangle au lieu de null
            shader = new Shader("default");
        } catch (Exception e) {
            System.err.println("Shader creation failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();

        // Vos vertices sont corrects pour un triangle 2D
        float[] vertices = {
                0.0f,  0.5f,   // sommet 0
                -0.5f, -0.5f,   // sommet 1
                0.5f, -0.5f     // sommet 2
        };

        // IMPORTANT : 3 indices pour 3 sommets
        int[] indices = {
                0, 1, 2  // UN SEUL triangle
        };

        // Le reste de votre code d'initialisation est correct...
        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        glBindVertexArray(VAO);

        glBindBuffer(GL30C.GL_ARRAY_BUFFER, VBO);
        glBufferData(GL30C.GL_ARRAY_BUFFER, vertexBuffer, GL30C.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL30C.GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30C.GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    public void render(Camera camera, float deltaTime) {
        try {
            shader.use();

            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("Error rendering triangle: " + e.getMessage());
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(VAO);
        shader.cleanup();
    }
}