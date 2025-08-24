package game.render;

import org.joml.Vector2f;
import org.joml.Vector3f;
import world.CoordSystem3D;
import world.MeshCube;

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
     * Convertit un MeshCube en rectangle 2D isométrique
     * @param meshCube Le cube 3D à projeter
     * @return Rectangle de rendu 2D
     */
    public static IsometricRect meshCubeToScreen(MeshCube meshCube) {
        Vector3f pos = new Vector3f(meshCube.position().x(), meshCube.position().y(), meshCube.position().z());
        Vector3f size = meshCube.size();

        // Calculer les 8 coins du cube en 3D
        Vector3f[] corners = calculateCubeCorners(pos, size);

        // Projeter tous les coins en 2D
        Vector2f[] screenCorners = new Vector2f[8];
        for (int i = 0; i < 8; i++) {
            screenCorners[i] = worldToScreen(corners[i]);
        }

        // Calculer la bounding box 2D
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (Vector2f corner : screenCorners) {
            minX = Math.min(minX, corner.x);
            maxX = Math.max(maxX, corner.x);
            minY = Math.min(minY, corner.y);
            maxY = Math.max(maxY, corner.y);
        }

        return new IsometricRect(
                minX, minY,
                maxX - minX, maxY - minY,
                screenCorners
        );
    }

    /**
     * Calcule les 8 coins d'un cube 3D
     * @param position Position du cube
     * @param size Taille du cube
     * @return Array des 8 coins
     */
    private static Vector3f[] calculateCubeCorners(Vector3f position, Vector3f size) {
        Vector3f[] corners = new Vector3f[8];

        // Face avant (z = position.z)
        corners[0] = new Vector3f(position.x, position.y, position.z);
        corners[1] = new Vector3f(position.x + size.x, position.y, position.z);
        corners[2] = new Vector3f(position.x + size.x, position.y + size.y, position.z);
        corners[3] = new Vector3f(position.x, position.y + size.y, position.z);

        // Face arrière (z = position.z + size.z)
        corners[4] = new Vector3f(position.x, position.y, position.z + size.z);
        corners[5] = new Vector3f(position.x + size.x, position.y, position.z + size.z);
        corners[6] = new Vector3f(position.x + size.x, position.y + size.y, position.z + size.z);
        corners[7] = new Vector3f(position.x, position.y + size.y, position.z + size.z);

        return corners;
    }

    /**
     * Convertit un MeshCube en losange de base (pour les blocs unitaires)
     * @param meshCube Le cube à projeter
     * @return Points du losange en coordonnées écran
     */
    public static Vector2f[] meshCubeToIsometricDiamond(MeshCube meshCube) {
        Vector3f pos = new Vector3f(meshCube.position().x(), meshCube.position().y(), meshCube.position().z());
        Vector3f size = meshCube.size();

        // Pour un cube unitaire, calculer les 4 points du losange de la face du dessus
        Vector3f topCenter = new Vector3f(pos.x + size.x/2, pos.y + size.y/2, pos.z + size.z);

        Vector3f[] diamondCorners3D = {
                new Vector3f(topCenter.x, topCenter.y - size.y/2, topCenter.z), // Nord
                new Vector3f(topCenter.x + size.x/2, topCenter.y, topCenter.z), // Est
                new Vector3f(topCenter.x, topCenter.y + size.y/2, topCenter.z), // Sud
                new Vector3f(topCenter.x - size.x/2, topCenter.y, topCenter.z)  // Ouest
        };

        Vector2f[] diamondCorners2D = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            diamondCorners2D[i] = worldToScreen(diamondCorners3D[i]);
        }

        return diamondCorners2D;
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
     * Calcule l'ordre de rendu pour un MeshCube
     */
    public static float getRenderDepth(MeshCube meshCube) {
        CoordSystem3D pos = meshCube.position();
        Vector3f size = meshCube.size();
        // Utiliser le coin le plus proche de la caméra pour le tri
        return (pos.y() + size.y) * 1000 + (pos.z() + size.z);
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

    /**
     * Vérifie si un MeshCube est visible à l'écran
     */
    public static boolean isVisible(MeshCube meshCube, float cameraX, float cameraY,
                                    int screenWidth, int screenHeight) {
        IsometricRect rect = meshCubeToScreen(meshCube);

        // Ajuster avec la position caméra
        float adjustedX = rect.x - cameraX;
        float adjustedY = rect.y - cameraY;

        // Marge pour les objets partiellement visibles
        int margin = TILE_WIDTH;

        return adjustedX + rect.width >= -margin &&
                adjustedX <= screenWidth + margin &&
                adjustedY + rect.height >= -margin &&
                adjustedY <= screenHeight + margin;
    }

    /**
     * Classe pour représenter un rectangle isométrique projeté
     */
    public static class IsometricRect {
        public final float x, y, width, height;
        public final Vector2f[] corners; // Les 8 coins projetés pour un rendu plus précis

        public IsometricRect(float x, float y, float width, float height, Vector2f[] corners) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.corners = corners;
        }

        public Vector2f getCenter() {
            return new Vector2f(x + width/2, y + height/2);
        }

        public boolean contains(float pointX, float pointY) {
            return pointX >= x && pointX <= x + width &&
                    pointY >= y && pointY <= y + height;
        }
    }
}