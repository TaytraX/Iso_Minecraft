package engine.render;

import org.joml.Vector2f;
import static org.lwjgl.opengl.GL11.*;

public class DisplayManager {
    private int windowWidth, windowHeight;
    private int renderWidth, renderHeight;
    private float aspectRatio;
    private final Vector2f renderOffset = new Vector2f(0, 0);
    private final Vector2f renderScale = new Vector2f(1, 1);

    // Résolution de référence pour ton jeu
    private static final int TARGET_WIDTH = 1280;
    private static final int TARGET_HEIGHT = 800;
    private static final float TARGET_ASPECT = (float) TARGET_WIDTH / TARGET_HEIGHT;

    public enum ScaleMode {
        STRETCH,    // Étire pour remplir l'écran
        LETTERBOX,  // Barres noires pour préserver l'aspect
        INTEGER     // Scaling par multiples entiers
    }

    private ScaleMode currentMode = ScaleMode.LETTERBOX;

    public void updateDisplay(int newWidth, int newHeight) {
        this.windowWidth = newWidth;
        this.windowHeight = newHeight;
        this.aspectRatio = (float) newWidth / newHeight;

        calculateRenderDimensions();
        updateViewport();
    }

    private void calculateRenderDimensions() {
        switch (currentMode) {
            case STRETCH -> {
                renderWidth = windowWidth;
                renderHeight = windowHeight;
                renderOffset.set(0, 0);
                renderScale.set(1, 1);
            }
            case LETTERBOX -> calculateLetterbox();
            case INTEGER -> calculateIntegerScale();
        }
    }

    private void calculateLetterbox() {
        if (aspectRatio > TARGET_ASPECT) {
            // Écran plus large : barres verticales
            renderHeight = windowHeight;
            renderWidth = (int) (windowHeight * TARGET_ASPECT);
            renderOffset.x = (windowWidth - renderWidth) / 2f;
            renderOffset.y = 0;
        } else {
            // Écran plus haut : barres horizontales
            renderWidth = windowWidth;
            renderHeight = (int) (windowWidth / TARGET_ASPECT);
            renderOffset.x = 0;
            renderOffset.y = (windowHeight - renderHeight) / 2f;
        }

        renderScale.x = (float) renderWidth / TARGET_WIDTH;
        renderScale.y = (float) renderHeight / TARGET_HEIGHT;
    }

    private void calculateIntegerScale() {
        int scaleX = windowWidth / TARGET_WIDTH;
        int scaleY = windowHeight / TARGET_HEIGHT;
        int scale = Math.max(1, Math.min(scaleX, scaleY));

        renderWidth = TARGET_WIDTH * scale;
        renderHeight = TARGET_HEIGHT * scale;

        renderOffset.x = (windowWidth - renderWidth) / 2f;
        renderOffset.y = (windowHeight - renderHeight) / 2f;

        renderScale.set(scale, scale);
    }

    private void updateViewport() {
        glViewport((int) renderOffset.x, (int) renderOffset.y,
                renderWidth, renderHeight);
    }

    public void clearBorders() {
        if (currentMode == ScaleMode.LETTERBOX || currentMode == ScaleMode.INTEGER) {
            glViewport(0, 0, windowWidth, windowHeight);
            glClearColor(0, 0, 0, 1); // Noir pour les barres
            glClear(GL_COLOR_BUFFER_BIT);
            updateViewport();
        }
    }

    // Getters pour la caméra
    public float getWorldWidth() { return TARGET_WIDTH / 64f; }  // Conversion en unités monde
    public float getWorldHeight() { return TARGET_HEIGHT / 64f; }
    public ScaleMode getScaleMode() { return currentMode; }
    public void setScaleMode(ScaleMode mode) {
        this.currentMode = mode;
        calculateRenderDimensions();
    }
}