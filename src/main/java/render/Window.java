package render;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;

public class Window {

    private final String title;

    private int width, height;
    private long window;
    private final boolean vSync;
    private DisplayManager displayManager;

    private boolean isFullscreen = false;
    private int windowedWidth, windowedHeight;
    private int windowedPosX, windowedPosY;

    public Window(String title, int width, int height, boolean vSync) {
        this.vSync = vSync;
        this.height = height;
        this.width = width;
        this.title = title;
        this.windowedWidth = width;
        this.windowedHeight = height;
    }

    public void init(){
        // Configuration des callbacks d'erreur
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3); // Changé de 2 à 3
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        boolean maximised = false;
        if(width == 0 || height == 0){
            width = 100;
            height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximised = true;
        }

        window = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if(window == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create GLFW window");

        // Définir le contexte AVANT createCapabilities()
        glfwMakeContextCurrent(window);

        // MAINTENANT on peut créer les capacités OpenGL
        createCapabilities();

        displayManager = new DisplayManager();
        displayManager.updateDisplay(width, height);

        // Vérification que OpenGL fonctionne
        if (glGetString(GL_VERSION) == null) {
            throw new RuntimeException("Failed to initialize OpenGL context");
        }

        // Callbacks après avoir créé le contexte
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            this.width = width;
            this.height = height;
            displayManager.updateDisplay(width, height);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            if(key == GLFW_KEY_F11 && action == GLFW_RELEASE)
                toggleFullscreen();
        });

        if(maximised){
            glfwMaximizeWindow(window);
        } else {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            assert vidMode != null;
            glfwSetWindowPos(window, (vidMode.width() - width) / 2,
                    (vidMode.height() - height) / 2);
        }

        if(isvSync())
            glfwSwapInterval(1);

        glfwShowWindow(window);
    }

    private void toggleFullscreen() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);

        if (!isFullscreen) {
            // Sauvegarder la position et taille actuelle
            int[] xPos = new int[1];
            int[] yPos = new int[1];
            glfwGetWindowPos(window, xPos, yPos);
            windowedPosX = xPos[0];
            windowedPosY = yPos[0];

            int[] w = new int[1];
            int[] h = new int[1];
            glfwGetWindowSize(window, w, h);
            windowedWidth = w[0];
            windowedHeight = h[0];

            // Passer en fullscreen
            assert vidMode != null;
            glfwSetWindowMonitor(window, monitor, 0, 0,
                    vidMode.width(), vidMode.height(),
                    vidMode.refreshRate());
            isFullscreen = true;
        } else {
            // Revenir en mode fenêtré
            glfwSetWindowMonitor(window, MemoryUtil.NULL,
                    windowedPosX, windowedPosY,
                    windowedWidth, windowedHeight, 0);
            isFullscreen = false;
        }

        // Réactiver vSync si nécessaire
        if(isvSync())
            glfwSwapInterval(1);
    }

    public void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void clear(){
        displayManager.clearBorders();
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void cleanup(){
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }

    public boolean windowShouldClose(){
        return glfwWindowShouldClose(window);
    }

    private boolean isvSync() {
        return vSync;
    }

    public long getWindowID() {
        return window; // La variable long window de la classe
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }
}