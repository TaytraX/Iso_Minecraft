package game;

import game.render.Renderer;

public class Engine {
    private Renderer renderer;
    public static boolean isRunning;

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