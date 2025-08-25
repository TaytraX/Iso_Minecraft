package game.render;

import window.Window;
import world.MeshCube;
import world.WorldManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    public final Window window;
    private final IsometricMeshRenderer meshRenderer;
    private final WorldManager worldManager;
    private final Camera camera;

    // Statistiques de rendu
    private int renderedCubesCount = 0;
    private long lastFpsTime = 0;
    private int fps = 0;
    private int frameCount = 0;

    public Renderer() {
        int height = 800;
        int width = 1280;
        window = new Window("Iso_Minecraft", width, height, true);

        meshRenderer = new IsometricMeshRenderer();
        worldManager = new WorldManager(3); // Distance de rendu de 3 chunks
        camera = new Camera(0, 0); // Caméra centrée

        init();
    }

    private void init() {
        // Configurer OpenGL
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.5f, 0.8f, 1.0f, 1.0f); // Couleur de fond bleu ciel

        meshRenderer.initialize();
        meshRenderer.updateScreenSize(1280, 800);

        // Centrer la caméra sur le monde
        camera.centerOnBlock(0, 0, 0);

        System.out.println("Renderer initialisé");
    }

    public void render() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFpsTime) / 1000.0f;

        // Mettre à jour les chunks en fonction de la position de la caméra
        worldManager.updateChunks(camera);

        // Effacer l'écran
        window.clear();

        // Obtenir les cubes visibles
        List<MeshCube> visibleCubes = worldManager.getVisibleMeshCubes(camera);
        renderedCubesCount = visibleCubes.size();

        // Ajouter tous les cubes à la file de rendu
        for (MeshCube cube : visibleCubes) {
            String textureName = getTextureForBlock(cube);
            meshRenderer.addToRenderQueue(cube, textureName);
        }

        // Effectuer le rendu
        meshRenderer.render(camera, deltaTime);

        // Afficher les statistiques (optionnel)
        updateFPS();

        // Mettre à jour la fenêtre
        window.update();
    }

    private String getTextureForBlock(MeshCube cube) {
        // Pour l'instant, on utilise une texture par défaut
        // Plus tard, vous pourrez associer différentes textures selon le type de bloc

        // Exemple : varier les textures selon la hauteur
        if (cube.position().z() == 0) {
            return "grass"; // Sol
        } else {
            return "dirt";  // Sous-sol
        }
    }

    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFpsTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = currentTime;

            // Afficher les stats dans la console (optionnel)
            if (fps % 60 == 0) { // Afficher toutes les 60 frames pour ne pas spammer
                System.out.println("FPS: " + fps + " | Cubes rendus: " + renderedCubesCount +
                        " | Chunks chargés: " + worldManager.getLoadedChunkCount());
            }
        }
    }

    public void handleInput(float deltaTime) {
        // Contrôles basiques de la caméra
        final float MOVE_SPEED = 200.0f; // pixels par seconde
        final float ZOOM_SPEED = 1.1f;

        long windowId = window.getWindowID();

        // Déplacement (WASD ou flèches)
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_W) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_UP)) {
            camera.move(0, -MOVE_SPEED * deltaTime);
        }
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_S) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN)) {
            camera.move(0, MOVE_SPEED * deltaTime);
        }
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_A) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT)) {
            camera.move(-MOVE_SPEED * deltaTime, 0);
        }
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_D) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT)) {
            camera.move(MOVE_SPEED * deltaTime, 0);
        }

        // Zoom (molette ou touches +/-)
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD)) {
            camera.zoom(ZOOM_SPEED);
        }
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS) ||
                isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT)) {
            camera.zoom(1.0f / ZOOM_SPEED);
        }

        // Retour au centre
        if (isKeyPressed(windowId, org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE)) {
            camera.centerOnBlock(0, 0, 0);
            camera.setZoom(1.0f);
        }
    }

    private boolean isKeyPressed(long windowId, int key) {
        return org.lwjgl.glfw.GLFW.glfwGetKey(windowId, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    public void cleanup() {
        meshRenderer.cleanup();
        worldManager.cleanup();
        window.cleanup();
    }

    // Getters pour l'accès externe
    public Camera getCamera() { return camera; }
    public WorldManager getWorldManager() { return worldManager; }
    public int getRenderedCubesCount() { return renderedCubesCount; }
    public int getFPS() { return fps; }
}