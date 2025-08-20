package render;

public interface Renderable {
    void initialize();
    void render(Camera camera, float deltaTime);
    void cleanup();
}