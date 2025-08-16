package systeme.startup;

import kotlin.io.AccessDeniedException;
import org.lwjgl.opengl.GL;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

public class SystemHardwareScanner {
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private final AtomicReference<SplashWindow> splashWindowRef;

    // Results thread-safe storage
    private volatile String cpuInfo = "Detecting...";
    private volatile String ramInfo = "Detecting...";
    private volatile String displayInfo = "Detecting...";
    private volatile String gpuInfo = "Detecting...";
    private volatile String openglInfo = "Waiting for OpenGL context...";
    private volatile String osInfo = "Detecting...";

    // Synchronization
    private final CountDownLatch openglLatch = new CountDownLatch(1);
    private volatile boolean detectionComplete = false;

    public SystemHardwareScanner(SplashWindow splashWindow) {
        SystemInfo systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.splashWindowRef = new AtomicReference<>(splashWindow);
    }

    // --- Détection OS (Thread-Safe) ---
    private void detectOS() {
        try {
            osInfo = String.format(
                    "%s %s %s",
                    os.getFamily(),
                    os.getVersionInfo().getVersion(),
                    os.getBitness() + "-bit"
            );
        } catch (Exception e) {
            osInfo = "Error - " + e.getMessage();
        }
    }

    // --- Détection CPU (Thread-Safe) ---
    private void detectCPU() {
        try {
            CentralProcessor processor = hardware.getProcessor();
            cpuInfo = String.format(
                    "%s (%d cores, %d threads)",
                    processor.getProcessorIdentifier().getName().trim(),
                    processor.getPhysicalProcessorCount(),
                    processor.getLogicalProcessorCount()
            );
        } catch (Exception e) {
            cpuInfo = "Error - " + e.getMessage();
        }
    }

    // --- Détection RAM (Thread-Safe) ---
    private void detectRAM() {
        try {
            GlobalMemory memory = hardware.getMemory();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            ramInfo = String.format(
                    "%.2f GB total (%.2f GB available)",
                    totalMemory / (1024.0 * 1024 * 1024),
                    availableMemory / (1024.0 * 1024 * 1024)
            );
        } catch (Exception e) {
            ramInfo = "Error - " + e.getMessage();
        }
    }

    // --- Détection des écrans (Thread-Safe, utilise AWT comme fallback) ---
    public void detectDisplays() {
        try {
            StringBuilder displayInfoBuilder = new StringBuilder("Displays:");

            // Méthode 1 : OSHI (peut ne pas marcher sur toutes les plateformes)
            try {
                var displays = hardware.getDisplays();
                if (!displays.isEmpty()) {
                    for (int i = 0; i < displays.size(); i++) {
                        // OSHI peut avoir des limitations selon la plateforme
                        displayInfoBuilder.append(String.format(
                                "- Display %d: OSHI detected", i + 1
                        ));
                    }
                }
            } catch (UnsupportedOperationException e) {
                // Plateforme non supportée par OSHI
                System.out.println("OSHI display detection not supported, using AWT fallback");
            } catch (SecurityException e) {
                displayInfo = "Displays: Access denied to display configuration";
                return;
            }

            // Méthode 2: AWT GraphicsEnvironment (plus fiable)
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] devices = ge.getScreenDevices();

                if (displayInfoBuilder.toString().equals("Displays:")) {
                    // Si OSHI n'a rien trouvé, utilise AWT
                    for (int i = 0; i < devices.length; i++) {
                        GraphicsDevice device = devices[i];
                        DisplayMode mode = device.getDisplayMode();
                        displayInfoBuilder.append(String.format(
                                "- Display %d: %dx%d @%dHz",
                                i + 1,
                                mode.getWidth(),
                                mode.getHeight(),
                                mode.getRefreshRate()
                        ));
                    }
                }
            } catch (Exception e) {
                displayInfoBuilder.append("- AWT display detection also failed");
            }

            displayInfo = displayInfoBuilder.toString();
        } catch (HeadlessException e) {
            // Système sans interface graphique
            displayInfo = "Headless environment detected";
        } catch (AWTError e) {
            // Erreur native AWT
            displayInfo = "Graphics subsystem error - " + e.getMessage();
        }
    }

    // --- Détection GPU (Thread-Safe, version simplifiée) ---
    public void detectGPU() {
        try {
            StringBuilder gpuInfoBuilder = new StringBuilder("GPU(s):");
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

            gpuInfo = gpuInfoBuilder.toString();
        } catch (Exception e) {
            gpuInfo = "GPU(s): Error - " + e.getMessage();
        }
    }

    // --- Détection OpenGL (Thread-Safe, doit être appelé depuis le thread OpenGL) ---
    public void detectOpenGLInfo() {
        try {
            // Vérifier d'abord si on a un contexte OpenGL valide
            if (!GL.getCapabilities().OpenGL11) {
                openglInfo = "OpenGL: No valid OpenGL 1.1+ context available";
                openglLatch.countDown();
                return;
            }

            String vendor = glGetString(GL_VENDOR);
            String renderer = glGetString(GL_RENDERER);
            String version = glGetString(GL_VERSION);
            String glslVersion = glGetString(GL_SHADING_LANGUAGE_VERSION);

            // Vérifier les erreurs OpenGL manuellement
            int error = glGetError();
            if (error != GL_NO_ERROR) {
                String errorMsg = getOpenGLErrorString(error);
                openglInfo = "OpenGL: Error " + error + " - " + errorMsg;
                openglLatch.countDown();
                return;
            }

            // Vérifier si les strings sont nulles (contexte corrompu)
            if (vendor == null || renderer == null || version == null) {
                openglInfo = "OpenGL: Invalid context - null strings returned";
                openglLatch.countDown();
                return;
            }

            openglInfo = String.format(
                    "OpenGL:- Vendor: %s- Renderer: %s- Version: %s- GLSL: %s",
                    vendor, renderer, version,
                    glslVersion != null ? glslVersion : "Not available"
            );
            openglLatch.countDown();

        } catch (IllegalStateException e) {
            // Pas de contexte OpenGL actif
            openglInfo = "OpenGL: No active OpenGL context";
            openglLatch.countDown();
        } catch (RuntimeException e) {
            // Crash JVM, corruption mémoire, driver crash
            openglInfo = "OpenGL: Runtime error - " + e.getMessage();
            openglLatch.countDown();
        }
    }

    private String getOpenGLErrorString(int error) {
        return switch (error) {
            case GL_INVALID_ENUM -> "Invalid enumeration";
            case GL_INVALID_VALUE -> "Invalid value";
            case GL_INVALID_OPERATION -> "Invalid operation";
            case GL_OUT_OF_MEMORY -> "Out of memory";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid framebuffer operation";
            default -> "Unknown error";
        };
    }

    // --- Méthode pour attendre la détection OpenGL ---
    public void waitForOpenGLDetection() throws InterruptedException {
        openglLatch.await();
    }

    // --- Affichage des résultats thread-safe ---
    public void printHardwareInfo() {
        System.out.println("=== Hardware Detection Results ===");
        System.out.println(osInfo);
        System.out.println(cpuInfo);
        System.out.println(ramInfo);
        System.out.println(displayInfo);
        System.out.println(gpuInfo);
        System.out.println(openglInfo);
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // bonne pratique
                System.err.println("Detection was interrupted.");
            } catch (IllegalArgumentException  | IllegalStateException e) {
                System.err.println("OpenGL detection failed: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Hardware detection error: " + e.getMessage());
            }
        });
    }

    // --- Sauvegarde thread-safe dans un fichier .config ---
    public void saveToConfigFile(String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            writer.write("# Hardware Detection Results\n");
            writer.write("# Generated on: " + java.time.LocalDateTime.now() + "\n\n");
            writer.write("OS = " + osInfo.replace("\n", "\\n") + "\n");
            writer.write("CPU = " + cpuInfo.replace("\n", "\\n") + "\n");
            writer.write("RAM = " + ramInfo.replace("\n", "\\n") + "\n");
            writer.write("Displays = " + displayInfo.replace("\n", "\\n") + "\n");
            writer.write("GPU = " + gpuInfo.replace("\n", "\\n"));
            writer.write("OpenGL = " + openglInfo.replace("\n", "\\n") + "\n");
            writer.write("DetectionComplete =" + detectionComplete + "\n");
            System.out.println("Hardware config saved to: " + filePath);
        }  catch (FileNotFoundException e) {
            System.err.println("Config file path not found: " + filePath);
        } catch (AccessDeniedException e) {
            System.err.println("Permission denied writing config to: " + filePath);
        } catch (IOException e) {
            System.err.println("I/O error saving config: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security policy prevents config file creation");
        }
    }

    // --- Getters thread safe pour l'état ---
    public String getOsInfo() { return osInfo; }
    public String getCpuInfo() { return cpuInfo; }
    public String getRamInfo() { return ramInfo; }
    public String getDisplayInfo() { return displayInfo; }
    public String getGpuInfo() { return gpuInfo; }
    public String getOpenglInfo() { return openglInfo; }
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