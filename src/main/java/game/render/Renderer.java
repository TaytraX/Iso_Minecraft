package game.render;

import game.render.renderers.MeshRender;
import window.Window;

public class Renderer {

    public final Window window;
    private final MeshRender meshRenderer;

    public Renderer() {
        int height = 800;
        int wigth = 1280;
        window = new Window("Iso_Minecraft", wigth, height, true);
        meshRenderer = new MeshRender();
        init();
    }

    private void init() {
        window.init();
        window.clear();
        meshRenderer.initialize();
    }

    public void render() {
        meshRenderer.render(null, 0);
        window.update();
    }

    public void cleanup() {
        window.cleanup();
        meshRenderer.cleanup();
    }
}