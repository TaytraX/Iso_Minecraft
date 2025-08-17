package systeme.startup;

import org.lwjgl.opengl.GL30C;
import render.loader.Shader;
import render.loader.Texture;
import systeme.exception.ShaderCompilationException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class SplashRenderer {
    private int VAO;
    private final Shader shader;
    private final Texture texture;

    public SplashRenderer() {
        try {
            shader = new Shader("splash");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            texture = new Texture("splash");
            initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        int textureVBO = glGenBuffers();

        float[] vertices = {
                -1.0f, -1.0f,  // Bas gauche
                1.0f, -1.0f,  // Bas droit
                1.0f,  1.0f,  // Haut droit
                -1.0f,  1.0f   // Haut gauche
        };

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
        };

        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        float[] textureCoords = {
                0.0f, 1.0f,  // Bas gauche -> correspond au haut de l'image
                1.0f, 1.0f,  // Bas droit -> correspond au haut de l'image
                1.0f, 0.0f,  // Haut droit -> correspond au bas de l'image
                0.0f, 0.0f   // Haut gauche -> correspond au bas de l'image
        };

        FloatBuffer textureBuffer = org.lwjgl.BufferUtils.createFloatBuffer(textureCoords.length);
        textureBuffer.put(textureCoords).flip();

        glBindVertexArray(VAO);

        glBindBuffer(GL30C.GL_ARRAY_BUFFER, VBO);
        glBufferData(GL30C.GL_ARRAY_BUFFER, vertexBuffer, GL30C.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL30C.GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL30C.GL_ARRAY_BUFFER, textureVBO);
        glBufferData(GL30C.GL_ARRAY_BUFFER, textureBuffer, GL30C.GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL30C.GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL30C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30C.GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    public void render() {
        try {
            shader.use();

            shader.getUniforms().setInt("splashTexture", 0);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getTextureID());

            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("Error rendering splash screen: " + e.getMessage());
        }
    }

    public void cleanup() {
        shader.cleanup();
        texture.cleanUp();
    }
}