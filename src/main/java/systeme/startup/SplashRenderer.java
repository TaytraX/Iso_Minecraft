package systeme.startup;

import org.lwjgl.BufferUtils;
import engine.render.loader.Shader;
import engine.render.loader.Texture;

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
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class SplashRenderer {
    private int VAO;
    int VBO, EBO, textureVBO;
    private final Shader splashShader;
    private final Texture splashTexture;

    public SplashRenderer() {
        System.out.println("Creating splashShader...");
        try {
            splashShader = new Shader("splash");
            System.out.println("Shader created successfully. Program ID: " + splashShader.programID);
        } catch (Exception e) {
            System.err.println("Shader creation failed: " + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            splashTexture = new Texture("splash");
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

    private void initialize() {
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        textureVBO = glGenBuffers();
        EBO = glGenBuffers();

        if (VAO == 0 || VBO == 0 || textureVBO == 0 || EBO == 0) {
            cleanup(); // Libérer ce qui a pu être alloué
            throw new RuntimeException("Failed to generate OpenGL objects");
        }


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
            glClear(GL_COLOR_BUFFER_BIT);
            splashShader.use();

            splashShader.getUniforms().setInt("splashTexture", 0);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, splashTexture.getTextureID());
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
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteBuffers(VBO);

        splashShader.cleanup();
        splashTexture.cleanUp();
    }
}
