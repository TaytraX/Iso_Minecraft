package systeme.startup;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;

public class SystemHardwareScanner {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private final AtomicReference<SplashWindow> splashWindowRef;

    // Results thread-safe storage
    private final AtomicReference<String> cpuInfo = new AtomicReference<>("Detecting...");
    private final AtomicReference<String> ramInfo = new AtomicReference<>("Detecting...");
    private final AtomicReference<String> displayInfo = new AtomicReference<>("Detecting...");
    private final AtomicReference<String> gpuInfo = new AtomicReference<>("Detecting...");
    private final AtomicReference<String> openglInfo = new AtomicReference<>("Waiting for OpenGL context...");
    private final AtomicReference<String> osInfo = new AtomicReference<>("Detecting...");

    // Synchronization
    private final CountDownLatch openglLatch = new CountDownLatch(1);
    private volatile boolean detectionComplete = false;

    public SystemHardwareScanner(SplashWindow splashWindow) {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.splashWindowRef = new AtomicReference<>(splashWindow);
    }

    public SystemHardwareScanner() {
        this(null);
    }

    // --- Détection OS (Thread-Safe) ---
    private void detectOS() {
        try {
            String result = String.format(
                    "OS: %s %s (%s)",
                    os.getFamily(),
                    os.getVersionInfo().getVersion(),
                    os.getBitness() + "-bit"
            );
            osInfo.set(result);
        } catch (Exception e) {
            String error = "OS: Error - " + e.getMessage();
            osInfo.set(error);
        }
    }

    // --- Détection CPU (Thread-Safe) ---
    private void detectCPU() {
        try {
            CentralProcessor processor = hardware.getProcessor();
            String result = String.format(
                    "CPU: %s (%d cores, %d threads)",
                    processor.getProcessorIdentifier().getName().trim(),
                    processor.getPhysicalProcessorCount(),
                    processor.getLogicalProcessorCount()
            );
            cpuInfo.set(result);
        } catch (Exception e) {
            String error = "CPU: Error - " + e.getMessage();
            cpuInfo.set(error);
        }
    }

    // --- Détection RAM (Thread-Safe) ---
    private void detectRAM() {
        try {
            GlobalMemory memory = hardware.getMemory();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            String result = String.format(
                    "RAM: %.2f GB total (%.2f GB available)",
                    totalMemory / (1024.0 * 1024 * 1024),
                    availableMemory / (1024.0 * 1024 * 1024)
            );
            ramInfo.set(result);
        } catch (Exception e) {
            String error = "RAM: Error - " + e.getMessage();
            ramInfo.set(error);
        }
    }

    // --- Détection des écrans (Thread-Safe, utilise AWT comme fallback) ---
    public void detectDisplays() {
        try {
            StringBuilder displayInfoBuilder = new StringBuilder("Displays:\n");

            // Méthode 1: OSHI (peut ne pas marcher sur toutes les plateformes)
            try {
                var displays = hardware.getDisplays();
                if (!displays.isEmpty()) {
                    for (int i = 0; i < displays.size(); i++) {
                        Display display = displays.get(i);
                        // OSHI peut avoir des limitations selon la plateforme
                        displayInfoBuilder.append(String.format(
                                "- Display %d: OSHI detected\n", i + 1
                        ));
                    }
                }
            } catch (Exception e) {
                System.out.println("OSHI display detection failed, using AWT fallback");
            }

            // Méthode 2: AWT GraphicsEnvironment (plus fiable)
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] devices = ge.getScreenDevices();

                if (displayInfoBuilder.toString().equals("Displays:\n")) {
                    // Si OSHI n'a rien trouvé, utilise AWT
                    for (int i = 0; i < devices.length; i++) {
                        GraphicsDevice device = devices[i];
                        DisplayMode mode = device.getDisplayMode();
                        displayInfoBuilder.append(String.format(
                                "- Display %d: %dx%d @%dHz\n",
                                i + 1,
                                mode.getWidth(),
                                mode.getHeight(),
                                mode.getRefreshRate()
                        ));
                    }
                }
            } catch (Exception e) {
                displayInfoBuilder.append("- AWT display detection also failed\n");
            }

            String result = displayInfoBuilder.toString();
            displayInfo.set(result);
        } catch (Exception e) {
            String error = "Displays: Error - " + e.getMessage();
            displayInfo.set(error);
        }
    }

    // --- Détection GPU (Thread-Safe, version simplifiée) ---
    public void detectGPU() {
        try {
            StringBuilder gpuInfoBuilder = new StringBuilder("GPU(s):\n");
            var cards = hardware.getGraphicsCards();

            if (cards.isEmpty()) {
                gpuInfoBuilder.append("- No graphics cards detected by OSHI\n");
            } else {
                for (GraphicsCard card : cards) {
                    String name = card.getName();
                    long vram = card.getVRam();

                    gpuInfoBuilder.append(String.format(
                            "- %s",
                            name != null && !name.trim().isEmpty() ? name.trim() : "Unknown GPU"
                    ));

                    if (vram > 0) {
                        gpuInfoBuilder.append(String.format(" (VRAM: %d MB)", vram / (1024 * 1024)));
                    }

                    gpuInfoBuilder.append("\n");
                }
            }

            String result = gpuInfoBuilder.toString();
            gpuInfo.set(result);
        } catch (Exception e) {
            String error = "GPU(s): Error - " + e.getMessage();
            gpuInfo.set(error);
        }
    }

    // --- Détection OpenGL (Thread-Safe, doit être appelé depuis le thread OpenGL) ---
    public void detectOpenGLInfo() {
        SplashWindow splashWindow = splashWindowRef.get();
        if (splashWindow == null) {
            String result = "OpenGL: SplashWindow not provided";
            openglInfo.set(result);
            openglLatch.countDown();
            return;
        }

        try {
            String vendor = glGetString(GL_VENDOR);
            String renderer = glGetString(GL_RENDERER);
            String version = glGetString(GL_VERSION);
            String glslVersion = glGetString(GL_SHADING_LANGUAGE_VERSION);

            String result = String.format(
                    "OpenGL:\n- Vendor: %s\n- Renderer: %s\n- Version: %s\n- GLSL: %s",
                    vendor != null ? vendor : "Unknown",
                    renderer != null ? renderer : "Unknown",
                    version != null ? version : "Unknown",
                    glslVersion != null ? glslVersion : "Unknown"
            );

            openglInfo.set(result);
            openglLatch.countDown();
        } catch (Exception e) {
            String error = "OpenGL: Error - " + e.getMessage();
            openglInfo.set(error);
            openglLatch.countDown();
        }
    }

    // --- Méthode pour attendre la détection OpenGL ---
    public void waitForOpenGLDetection() throws InterruptedException {
        openglLatch.await();
    }

    // --- Affichage des résultats thread-safe ---
    public void printHardwareInfo() {
        System.out.println("=== Hardware Detection Results ===");
        System.out.println(osInfo.get());
        System.out.println(cpuInfo.get());
        System.out.println(ramInfo.get());
        System.out.println(displayInfo.get());
        System.out.println(gpuInfo.get());
        System.out.println(openglInfo.get());
        System.out.println("==================================");
    }

    // --- Exécution asynchrone de la détection ---
    public CompletableFuture<Void> runDetectionAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Starting hardware detection...");

                // Détection des composants non-OpenGL
                detectOS();
                detectCPU();
                detectRAM();
                detectDisplays();
                detectGPU();

                System.out.println("Hardware detection complete, waiting for OpenGL...");

                // Attendre la détection OpenGL (faite depuis le thread principal)
                waitForOpenGLDetection();

                detectionComplete = true;
                System.out.println("All detection complete!");
                printHardwareInfo();
            } catch (Exception e) {
                System.err.println("Error during hardware detection: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // --- Méthode synchrone pour compatibilité ---
    public void runDetection() {
        detectOS();
        detectCPU();
        detectRAM();
        detectDisplays();
        detectGPU();

        // Pour OpenGL, on doit être sur le bon thread
        if (Thread.currentThread().getName().contains("main") ||
                Thread.currentThread().getName().contains("OpenGL")) {
            detectOpenGLInfo();
        }

        detectionComplete = true;
        printHardwareInfo();
    }

    // --- Sauvegarde thread-safe dans un fichier .config ---
    public synchronized void saveToConfigFile(String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            writer.write("# Hardware Detection Results\n");
            writer.write("# Generated on: " + java.time.LocalDateTime.now() + "\n\n");
            writer.write("OS=" + osInfo.get().replace("\n", "\\n") + "\n");
            writer.write("CPU=" + cpuInfo.get().replace("\n", "\\n") + "\n");
            writer.write("RAM=" + ramInfo.get().replace("\n", "\\n") + "\n");
            writer.write("Displays=" + displayInfo.get().replace("\n", "\\n") + "\n");
            writer.write("GPU=" + gpuInfo.get().replace("\n", "\\n") + "\n");
            writer.write("OpenGL=" + openglInfo.get().replace("\n", "\\n") + "\n");
            writer.write("DetectionComplete=" + detectionComplete + "\n");
            System.out.println("Hardware config saved to: " + filePath);
        } catch (java.io.IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    // --- Getters thread-safe pour l'état ---
    public String getOsInfo() { return osInfo.get(); }
    public String getCpuInfo() { return cpuInfo.get(); }
    public String getRamInfo() { return ramInfo.get(); }
    public String getDisplayInfo() { return displayInfo.get(); }
    public String getGpuInfo() { return gpuInfo.get(); }
    public String getOpenglInfo() { return openglInfo.get(); }
    public boolean isDetectionComplete() { return detectionComplete; }

    // --- Méthode pour déclencher la détection OpenGL depuis le thread principal ---
    public void triggerOpenGLDetection() {
        detectOpenGLInfo();
    }

    // --- Méthode pour mettre à jour la référence SplashWindow ---
    public void setSplashWindow(SplashWindow splashWindow) {
        this.splashWindowRef.set(splashWindow);
    }
}