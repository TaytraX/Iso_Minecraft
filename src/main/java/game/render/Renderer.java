package game.render;

import window.Window;

public class Renderer {

    public final Window window;

    public Renderer() {
        int height = 800;
        int width = 1280;
        window = new Window("Iso_Minecraft", width, height, true);
        init();
    }

    private void init() {
    }

    public void render() {
        // Mettre à jour la fenêtre
        window.update();
    }

    public void cleanup() {
        window.cleanup();
    }
}