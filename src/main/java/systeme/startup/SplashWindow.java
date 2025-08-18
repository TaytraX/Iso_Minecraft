package systeme.startup;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class SplashWindow {
    private long window;

    public void init() {
        initGLFW();
        splashConfig();
        createSplashWindow();
    }

    private void initGLFW(){
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
    }

    private void splashConfig() {
        glfwDefaultWindowHints();

        //Visibilité and Comportement
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);

        //Configuration OpenGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
    }

    private void createSplashWindow() {
        int width = 1200;
        int height = 700;

        window = glfwCreateWindow(width, height, "Iso_Minecraft", 0, 0);

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window. OpenGL " +
                    3 + "." + 3 +
                    " might not be supported.");
        }

        glfwMakeContextCurrent(window);
        createCapabilities();

        // Validation OpenGL
        String glVersion = glGetString(GL_VERSION);
        if (glVersion == null) {
            throw new RuntimeException("Failed to initialize OpenGL context");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidMode != null;
        glfwSetWindowPos(window, (vidMode.width() - width) / 2,
                (vidMode.height() - height) / 2);

        glfwShowWindow(window);
    }

    public void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void cleanup(){
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }
}