package game.render;

import game.render.renderers.IsometricMeshRenderer;
import window.Window;
import world.MeshCube;

import static world.Chunk.chunk;

public class Renderer {

    public final Window window;
    private final IsometricMeshRenderer meshRenderer;

    public Renderer() {
        int height = 800;
        int wigth = 1280;
        window = new Window("Iso_Minecraft", wigth, height, true);
        meshRenderer = new IsometricMeshRenderer();
        init();
    }

    private void init() {
        meshRenderer.initialize();
    }

    public void render() {
        window.clear();
        for (MeshCube cube : chunk.values()) {
            meshRenderer.addToRenderQueue(cube, "grass");
        }
        meshRenderer.render(null, 0);
        window.update();
    }

    public void cleanup() {
        window.cleanup();
        meshRenderer.cleanup();
    }
}