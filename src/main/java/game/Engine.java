package game;

import game.render.Renderer;
import overworld.world.*;

public class Engine {
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