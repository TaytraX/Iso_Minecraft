package game;

import game.render.Renderer;
import org.joml.Vector2f;
import world.CoordSystem3D;

public class Engine {
    private Renderer renderer;
    private long lastTime;
    private float deltaTime;

    public void init() {
        renderer = new Renderer();
        lastTime = System.nanoTime();

        // Test de projection pour vérifier que tout fonctionne
        CoordSystem3D testCoord = new CoordSystem3D(5, 3, 2);
        Vector2f screenPos = IsometricProjection.coordToScreen(testCoord);
        System.out.println("Test projection - Bloc (5,3,2) → Écran: " + screenPos);

        System.out.println("Engine initialisé");
    }

    public void start() {
        init();
        run();
    }

    public void run() {
        System.out.println("Démarrage de la boucle principale...");
        System.out.println("Contrôles:");
        System.out.println("  WASD ou flèches : Déplacer la caméra");
        System.out.println("  +/- : Zoomer/Dézoomer");
        System.out.println("  Espace : Retour au centre");
        System.out.println("  Échap : Quitter");
        System.out.println("  F11 : Plein écran");

        while(!renderer.window.windowShouldClose()) {
            // Calculer le deltaTime
            long currentTime = System.nanoTime();
            deltaTime = (currentTime - lastTime) / 1_000_000_000.0f; // Convertir en secondes
            lastTime = currentTime;

            // Limiter le deltaTime pour éviter les gros saut de temps
            deltaTime = Math.min(deltaTime, 1.0f / 30.0f); // Maximum 30 FPS minimum

            // Gérer les entrées utilisateur
            renderer.handleInput(deltaTime);

            // Effectuer le rendu
            renderer.render();
        }

        cleanup();
    }

    private void cleanup() {
        System.out.println("Nettoyage en cours...");
        renderer.cleanup();
        System.out.println("Application fermée proprement");
    }

    // Getters pour l'accès externe (si besoin)
    public Renderer getRenderer() { return renderer; }
    public float getDeltaTime() { return deltaTime; }
}