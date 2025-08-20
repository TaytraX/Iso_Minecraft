package game;

import game.render.Renderer;

public class Engine {
    private int FPS;
    private Renderer renderer;

    public void init() {
        renderer = new Renderer();
    }

    public void start() {
        init();
        run();
    }

    public void run() {
        while(!renderer.window.windowShouldClose()) {
            renderer.render();
        }
        cleanup();
    }

    private void cleanup() {
        renderer.cleanup();
    }
}