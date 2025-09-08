package game.render;

import game.render.renderers.WorldRender;
import window.Window;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    public final Window window;
    public final WorldRender worldRender;
    private final Camera camera;
    float deltaTime;
    private double lastTime;

    public Renderer() {
        int height = 800;
        int width = 1280;
        window = new Window("Iso_Minecraft", width, height, true);
        worldRender = new WorldRender();
        camera = new Camera(deltaTime, (float) width / height);
        init();
    }

    private void init() {
        camera.init();
        worldRender.initialize();
    }

    public void render() {
        // Calcul du deltaTime
        double currentTime = glfwGetTime();
        if (lastTime == 0.0) lastTime = currentTime;

        deltaTime = (float)(currentTime - lastTime);
        lastTime = currentTime;

        camera.setDeltaTime(deltaTime);

        clear();
        worldRender.render(camera);
        window.update();
        camera.update(window);
    }

    private void clear() {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the framebuffer
    }

    public void cleanup() {
        window.cleanup();
        worldRender.cleanup();
    }
}