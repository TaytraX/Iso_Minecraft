package game.render;

import org.joml.Vector2f;
import org.joml.Vector3f;
import world.CoordSystem3D;

public class IsometricProjection {

    // Taille des tuiles en pixels
    public static final int TILE_WIDTH = 64;   // Largeur du losange
    public static final int TILE_HEIGHT = 32;  // Hauteur du losange

    /**
     * Convertit les coordonnées 3D du monde en coordonnées écran isométriques
     * @param coord Coordonnées 3D du bloc (entiers)
     * @return Position écran (x, y)
     */
    public static Vector2f coordToScreen(CoordSystem3D coord) {
        float screenX = (coord.x() - coord.y()) * (TILE_WIDTH / 2.0f);
        float screenY = (coord.x() + coord.y()) * (TILE_HEIGHT / 2.0f) - coord.z() * TILE_HEIGHT;

        return new Vector2f(screenX, screenY);
    }

    /**
     * Convertit une position d'entité (Vector3f) en coordonnées écran
     * @param position Position continue de l'entité
     * @return Position écran (x, y)
     */
    public static Vector2f worldToScreen(Vector3f position) {
        float screenX = (position.x - position.y) * (TILE_WIDTH / 2.0f);
        float screenY = (position.x + position.y) * (TILE_HEIGHT / 2.0f) - position.z * TILE_HEIGHT;

        return new Vector2f(screenX, screenY);
    }

    /**
     * Convertit les coordonnées écran vers les coordonnées monde (pour la souris)
     * @param screenX Position X à l'écran
     * @param screenY Position Y à l'écran
     * @param z Hauteur assumée (généralement 0 pour le sol)
     * @return Coordonnées monde
     */
    public static Vector3f screenToWorld(float screenX, float screenY, float z) {
        float worldX = (screenX / (TILE_WIDTH / 2.0f) + screenY / (TILE_HEIGHT / 2.0f)) / 2.0f;
        float worldY = (screenY / (TILE_HEIGHT / 2.0f) - screenX / (TILE_WIDTH / 2.0f)) / 2.0f;

        return new Vector3f(worldX, worldY, z);
    }

    /**
     * Calcule l'ordre de rendu (depth sorting) pour l'isométrique
     * Les objets plus loin en Y et plus bas en Z sont rendus en premier
     */
    public static float getRenderDepth(CoordSystem3D coord) {
        return coord.y() * 1000 + coord.z();
    }

    /**
     * Vérifie si un bloc est visible à l'écran (frustum culling basique)
     */
    public static boolean isVisible(CoordSystem3D coord, float cameraX, float cameraY,
                                    int screenWidth, int screenHeight) {
        Vector2f screenPos = coordToScreen(coord);

        // Ajouter la position caméra
        screenPos.x -= cameraX;
        screenPos.y -= cameraY;

        // Marge pour les blocs partiellement visibles
        int margin = TILE_WIDTH;

        return screenPos.x >= -margin &&
                screenPos.x <= screenWidth + margin &&
                screenPos.y >= -margin &&
                screenPos.y <= screenHeight + margin;
    }
}