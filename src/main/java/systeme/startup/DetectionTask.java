package systeme.startup;

import oshi.SystemInfo;
import oshi.hardware.*;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;

public class DetectionTask {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final SplashWindow splashWindow;

    public DetectionTask(SplashWindow splashWindow) {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.splashWindow = splashWindow;
    }

    // --- Détection CPU ---
    public String detectCPU() {
        CentralProcessor processor = hardware.getProcessor();
        return String.format(
                "CPU: %s (%d cores, %d threads)",
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount()
        );
    }

    // --- Détection RAM ---
    public String detectRAM() {
        long totalMemory = hardware.getMemory().getTotal();
        return String.format("RAM: %.2f GB", totalMemory / (1024.0 * 1024 * 1024));
    }

    // --- Détection des écrans ---
    public String detectDisplays() {
        List<Display> displays = hardware.getDisplays();
        StringBuilder displayInfo = new StringBuilder("Displays:\n");
        for (int i = 0; i < displays.length; i++) {
            displayInfo.append(String.format(
                    "- Display %d: %dx%d\n",
                    i + 1,
                    displays.get(i).getNativeMode().getResolution().getWidth(),
                    displays[i].getResolution().getHeight()
            ));
        }
        return displayInfo.toString();
    }

    // --- Détection GPU ---
    public String detectGPU() {
        StringBuilder gpuInfo = new StringBuilder("GPU(s):\n");
        for (GraphicsCard card : hardware.getGraphicsCards()) {
            gpuInfo.append(String.format(
                    "- %s (VRAM: %d MB, Driver: %s)\n",
                    card.getName(),
                    card.getVRam(),
                    card.getDriverVersion()
            ));
        }
        return gpuInfo.toString();
    }

    // --- Détection OpenGL (via SplashWindow) ---
    public String detectOpenGLInfo() {
        if (splashWindow == null) {
            return "OpenGL: SplashWindow not provided";
        }
        try {
            String vendor = glGetString(GL_VENDOR);
            String renderer = glGetString(GL_RENDERER);
            String version = glGetString(GL_VERSION);
            String glslVersion = glGetString(GL_SHADING_LANGUAGE_VERSION);
            return String.format(
                    "OpenGL:\n- Vendor: %s\n- Renderer: %s\n- Version: %s\n- GLSL: %s",
                    vendor, renderer, version, glslVersion
            );
        } catch (Exception e) {
            return "OpenGL: Error - " + e.getMessage();
        }
    }

    // --- Affichage des résultats ---
    public void printHardwareInfo() {
        System.out.println("=== Hardware Detection ===");
        System.out.println(detectCPU());
        System.out.println(detectRAM());
        System.out.println(detectDisplays());
        System.out.println(detectGPU());
        System.out.println(detectOpenGLInfo());
        System.out.println("===========================");
    }

    // --- Exécution de la détection ---
    public void runDetection() {
        try {
            printHardwareInfo();
        } catch (Exception e) {
            System.err.println("Error during hardware detection: " + e.getMessage());
        }
    }

    // --- Sauvegarde dans un fichier .config ---
    public void saveToConfigFile(String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            writer.write("# Hardware Detection Results\n");
            writer.write("CPU=" + detectCPU() + "\n");
            writer.write("RAM=" + detectRAM() + "\n");
            writer.write("Displays=" + detectDisplays().replace("\n", "\\n") + "\n");
            writer.write("GPU=" + detectGPU().replace("\n", "\\n") + "\n");
            writer.write("OpenGL=" + detectOpenGLInfo().replace("\n", "\\n") + "\n");
            System.out.println("Config saved to: " + filePath);
        } catch (java.io.IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }
}