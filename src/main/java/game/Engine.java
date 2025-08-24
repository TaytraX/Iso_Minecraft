package game;

import game.render.IsometricProjection;
import game.render.Renderer;
import org.joml.Vector2f;
import world.CoordSystem3D;

public class Engine {
    private Renderer renderer;

    public void init() {
        renderer = new Renderer();

        // Test de projection
        CoordSystem3D testCoord = new CoordSystem3D(5, 3, 2);
        Vector2f screenPos = IsometricProjection.coordToScreen(testCoord);
        System.out.println("Bloc (5,3,2) → Écran: " + screenPos);
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