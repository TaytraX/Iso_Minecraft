package systeme.startup;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
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
        System.out.println("Creating shader...");
        try {
            shader = new Shader("splash");
            System.out.println("Shader created successfully. Program ID: " + shader.programID);
        } catch (Exception e) {
            System.err.println("Shader creation failed: " + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            texture = new Texture("splash");
        } catch (Exception e) {
            System.err.println("Texture creation failed: " + e.getMessage());
            throw new RuntimeException(e);
        }

        System.out.println("Initializing VAO...");
        try {
            initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        VAO = glGenVertexArrays();
        if (VAO == 0) throw new RuntimeException("Failed to generate VAO");

        int VBO = glGenBuffers();
        if (VBO == 0) throw new RuntimeException("Failed to generate VBO");

        int textureVBO = glGenBuffers();
        if (textureVBO == 0) throw new RuntimeException("Failed to generate textureVBO");

        int EBO = glGenBuffers();
        if (EBO == 0) throw new RuntimeException("Failed to generate EBO");


        float[] vertices = {
                -1.0f, -1.0f,
                 1.0f, -1.0f,
                 1.0f,  1.0f,
                -1.0f,  1.0f
        };

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();



        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();


        float[] textureCoords = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f
        };
        FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
        textureBuffer.put(textureCoords).flip();


        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, textureVBO);
        glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

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
            e.printStackTrace();
        }
    }
    public void cleanup() {
        shader.cleanup();
        texture.cleanUp();
    }
}
