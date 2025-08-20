package engine.render;

import window.Window;

public class Renderer {

    private Window window;
    private int wigth = 1280;
    private int height = 800;

    public Renderer() {
        window = new Window("Iso_Minecraft", wigth, height, true);
    }

    public void init() {
        window.init();
    }
}