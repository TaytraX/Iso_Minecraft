package game.render.renderers;

import game.render.Camera;
import game.render.loader.Shader;
import game.render.loader.Texture;
import systeme.exception.ShaderCompilationException;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class WorldRender implements GameRenderable {
    int VAO, VBO, EBO, TextureVBO;
    private final Shader shader;
    private final Texture texture;

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

    }

    @Override
    public void initialize() {

    }

    @Override
    public void render(Camera camera, float deltaTime) {

    }

    @Override
    public void cleanup() {
        shader.cleanUp();
        texture.cleanUp();
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteBuffers(TextureVBO);
    }
}