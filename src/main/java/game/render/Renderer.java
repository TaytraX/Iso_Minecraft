package game.render;

import game.render.renderers.WorldRender;
import window.Window;

public class Renderer {
    public final Window window;
    private final WorldRender worldRender;
    private Camera camera;

    public Renderer() {
        int height = 800;
        int width = 1280;
        window = new Window("Iso_Minecraft", width, height, true);
        worldRender = new WorldRender();
        camera = new Camera();
        init();
    }

    private void init() {
        worldRender.initialize();
    }

    public void render() {
        window.clear();
        worldRender.render(camera, 0);
        window.update();
    }

    public void cleanup() {
        window.cleanup();
    }
}