package game.render.renderers;

import game.render.Camera;
import game.render.GameRenderable;
import game.render.loader.Shader;
import org.lwjgl.opengl.GL30C;
import systeme.exception.ShaderCompilationException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class MeshRender implements GameRenderable {

    private int VAO, VBO, EBO;
    private final Shader shader;

    public MeshRender() {
        try {
            shader = new Shader(null);
        } catch (ShaderCompilationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize() {

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        float[] vertices = {
                -1.0f, -1.0f,  // Bas gauche
                1.0f, -1.0f,  // Bas droit
                1.0f,  1.0f,  // Haut droit
                -1.0f,  1.0f   // Haut gauche
        };

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        // Ordre des indices correct pour le sens antihoraire
        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
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

    @Override
    public void render(Camera camera, float deltaTime) {
        try {

            shader.use();
            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("Erreur dans le rendu du background" + e);
        }
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteVertexArrays(VAO);
    }
}