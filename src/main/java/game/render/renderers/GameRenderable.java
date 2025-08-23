package game.render.renderers;

import game.render.Camera;

public interface GameRenderable {
    void initialize();
    void render(Camera camera, float deltaTime);
    void cleanup();
}