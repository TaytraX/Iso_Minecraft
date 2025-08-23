package game;

import game.render.Renderer;
import world.Test;

public class Engine {
    private Renderer renderer;
    Test test;

    public void init() {
        test = new Test();
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
        test.test();
    }

    private void cleanup() {
        renderer.cleanup();
    }
}