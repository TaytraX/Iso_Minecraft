package game.render.renderers;

import game.render.Camera;
import game.render.GameRenderable;
import game.render.loader.Shader;

import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class MeshRender implements GameRenderable {

    private int VAO;
    private final Shader shader;

    public MeshRender() {
        try {
            shader = new Shader(null);
        } catch (Exception e) {
            System.err.println("Shader creation failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void initialize() {

        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();

        // Géométrie du joueur (quad 2D)
        float[] vertices = {
                // x, y
                0.0f,  0.5f,   // sommet 0
                -0.5f, -0.5f,   // sommet 1
                0.5f, -0.5f    // sommet 2
        };
        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        int[] indices = {
                0, 1, 2  // ordre des sommets pour former le triangle
        };
        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        glBindVertexArray(VAO);

        glBindBuffer(GL30C.GL_ARRAY_BUFFER, VBO);
        glBufferData(GL30C.GL_ARRAY_BUFFER, vertexBuffer, GL30C.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL30C.GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL30C.GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL30C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30C.GL_STATIC_DRAW);

        glBindVertexArray(0);

    }

    public void render(Camera camera, float deltaTime) {
        try {
            glClear(GL_COLOR_BUFFER_BIT);
            shader.use();

            shader.getUniforms().setInt("splashTexture", 0);
            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("Error rendering splash screen: " + e.getMessage());
        }
    }

    public void cleanup() {
        // Cleanup splash
        glDeleteVertexArrays(VAO);

        shader.cleanup();
    }
}