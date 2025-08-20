package game.render;

public interface GameRenderable {
    void initialize();
    void render(Camera camera, float deltaTime);
    void cleanup();
}