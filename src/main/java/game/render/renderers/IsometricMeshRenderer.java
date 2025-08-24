package game.render.renderers;

import game.render.Camera;
import game.render.IsometricProjection;
import game.render.IsometricProjection.IsometricRect;
import game.render.loader.Shader;
import game.render.loader.Texture;
import org.joml.Vector2f;
import systeme.exception.ShaderCompilationException;
import world.CoordSystem3D;
import world.MeshCube;
import world.block.Dearth;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.*;

public class IsometricMeshRenderer implements GameRenderable {

    private int VAO, VBO, EBO;
    private final Shader shader;
    private Texture defaultTexture;
    Dearth block = new Dearth(new CoordSystem3D(0,0,0));

    // Liste des cubes à rendre
    private final List<RenderableMeshCube> renderQueue = new ArrayList<>();

    public IsometricMeshRenderer() {
        try {
            shader = new Shader("isometric_cube");
        } catch (ShaderCompilationException e) {
            throw new RuntimeException("Erreur lors du chargement du shader isométrique", e);
        }
    }

    @Override
    public void initialize() {
        setupBuffers();
        try {
            defaultTexture = new Texture(block.getTextureName());
        } catch (Exception e) {
            System.err.println("Erreur chargement texture par défaut: " + e.getMessage());
        }
    }

    private void setupBuffers() {
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        glBindVertexArray(VAO);

        // Buffer pour les sommets (position + coordonnées texture)
        glBindBuffer(GL_ARRAY_BUFFER, VBO);

        // Position (x, y) - attribut 0
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Coordonnées texture (u, v) - attribut 1
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBindVertexArray(0);
    }

    /**
     * Ajoute un MeshCube à la file de rendu
     */
    public void addToRenderQueue(MeshCube meshCube, String textureName) {
        float depth = IsometricProjection.getRenderDepth(meshCube);
        renderQueue.add(new RenderableMeshCube(meshCube, textureName, depth));
    }

    /**
     * Ajoute une liste de MeshCube à la file de rendu
     */
    public void addToRenderQueue(List<MeshCube> meshCubes, String textureName) {
        for (MeshCube cube : meshCubes) {
            addToRenderQueue(cube, textureName);
        }
    }

    @Override
    public void render(Camera camera, float deltaTime) {
        if (renderQueue.isEmpty()) return;

        // Trier par profondeur (les plus éloignés en premier)
        Collections.sort(renderQueue);

        shader.use();
        glBindVertexArray(VAO);

        // Rendu de chaque cube
        for (RenderableMeshCube renderable : renderQueue) {
            renderSingleCube(renderable, camera);
        }

        glBindVertexArray(0);
        shader.stop();

        // Vider la queue après rendu
        renderQueue.clear();
    }

    private void renderSingleCube(RenderableMeshCube renderable, Camera camera) {
        MeshCube cube = renderable.meshCube;

        // Calculer la projection isométrique
        float[] diamondPoints = IsometricProjection.meshCubeToIsometricDiamond(cube);

        // Créer les sommets du losange avec coordonnées texture
        float[] vertices = createDiamondVertices(diamondPoints);
        int[] indices = {0, 1, 2, 0, 2, 3}; // Deux triangles formant le losange

        // Uploader les données
        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);

        // Lier la texture (si disponible)
        if (defaultTexture != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, defaultTexture.getTextureID());
            shader.getUniforms().setInt("u_texture", 0);
        }

        // Dessiner
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
    }

    /**
     * Crée les sommets pour un losange isométrique
     */
    private float[] createDiamondVertices(Vector2f[] diamondPoints) {
        // Format: x, y, u, v pour chaque sommet
        return new float[] {
                // Nord
                diamondPoints[0].x, diamondPoints[0].y, 0.5f, 0.0f,
                // Est
                diamondPoints[1].x, diamondPoints[1].y, 1.0f, 0.5f,
                // Sud
                diamondPoints[2].x, diamondPoints[2].y, 0.5f, 1.0f,
                // Ouest
                diamondPoints[3].x, diamondPoints[3].y, 0.0f, 0.5f
        };
    }

    /**
     * Méthode pour rendre un cube 3D complet (avec faces latérales)
     */
    public void renderFullCube(MeshCube cube, Camera camera) {
        IsometricRect rect = IsometricProjection.meshCubeToScreen(cube);

        // Ici vous pourriez implémenter le rendu des 3 faces visibles
        // (dessus, côté droit, côté gauche) pour un effet 3D complet

        // Pour l'instant, on utilise juste le losange du dessus
        Vector2f[] diamondPoints = IsometricProjection.meshCubeToIsometricDiamond(cube);

        // ... logique de rendu ...
    }

    /**
     * Vérifie si un cube doit être rendu (culling)
     */
    public boolean shouldRender(MeshCube cube, Camera camera, int screenWidth, int screenHeight) {
        // Culling basé sur la visibilité
        return IsometricProjection.isVisible(cube, 0, 0, screenWidth, screenHeight);
    }

    @Override
    public void cleanup() {
        if (defaultTexture != null) {
            defaultTexture.cleanUp();
        }
        shader.cleanup();
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
    }

    /**
     * Classe interne pour gérer les cubes avec leur profondeur de rendu
     */
    private static class RenderableMeshCube implements Comparable<RenderableMeshCube> {
        final MeshCube meshCube;
        final String textureName;
        final float renderDepth;

        RenderableMeshCube(MeshCube meshCube, String textureName, float renderDepth) {
            this.meshCube = meshCube;
            this.textureName = textureName;
            this.renderDepth = renderDepth;
        }

        @Override
        public int compareTo(RenderableMeshCube other) {
            return Float.compare(this.renderDepth, other.renderDepth);
        }
    }
}