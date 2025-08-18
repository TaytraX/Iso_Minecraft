package systeme.startup;

import systeme.filesystem.GameDirectoryManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Startup {
    GameDirectoryManager gameDirectoryManager = new GameDirectoryManager();
    private final SplashWindow splash;
    private SystemHardwareScanner HardwareScanner;
    private SplashRenderer splashRenderer;
    private final ExecutorService executorService;
    private CompletableFuture<Void> detectionFuture;

    public Startup() {
        splash = new SplashWindow();
        // Thread pool pour les tâches asynchrones
        executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "SystemHardwareScanner-Thread");
            t.setDaemon(true); // Thread daemon pour ne pas bloquer l'arrêt
            return t;
        });
    }

    public void init() {
        System.out.println("Initializing Splash Window...");
        splash.init();

        System.out.println("Initializing Splash Renderer...");
        splashRenderer = new SplashRenderer();

        System.out.println("Starting hardware detection...");
        // Créer SystemHardwareScanner après l'initialisation de SplashWindow
        HardwareScanner = new SystemHardwareScanner(splash);

        // Lancer la détection asynchrone
        detectionFuture = HardwareScanner.runDetectionAsync();
    }

    public void run() {
        init();
        loop();
        gameDirectoryManager.createGameDirectories();
        cleanup();
    }

    public void loop() {
        int frameCount = 0;
        boolean openglDetectionTriggered = false;

        while (!splash.shouldClose()) {
            update();
            render();

            // Déclencher la détection OpenGL après quelques frames
            // pour s'assurer que le contexte OpenGL est stable
            if (!openglDetectionTriggered && frameCount > 3) {
                HardwareScanner.triggerOpenGLDetection();
                openglDetectionTriggered = true;
                System.out.println("OpenGL detection triggered from main thread");
            }

            frameCount++;

            // Afficher le progrès de la détection
            if (frameCount % 60 == 0) { // Toutes les 60 frames environ
                printDetectionProgress();
            }

            // Sortir de la boucle si la détection est terminée et qu'on a attendu un peu
            if (HardwareScanner.isDetectionComplete() && frameCount > 300) {
                System.out.println("Detection complete, exiting splash...");
                break;
            }
        }
    }

    public void render() {
        splashRenderer.render();
    }

    public void update() {
        splash.update();
    }

    private void printDetectionProgress() {
        if (!HardwareScanner.isDetectionComplete()) {
            System.out.println("Detection in progress...");
            System.out.println("- " + HardwareScanner.getCpuInfo());
            System.out.println("- " + HardwareScanner.getRamInfo());
            System.out.println("- " + HardwareScanner.getGpuInfo());
            System.out.println("- " + HardwareScanner.getOpenglInfo());
        }
    }

    public void cleanup() {
        System.out.println("Cleaning up...");

        // Attendre la fin de la détection si elle n'est pas terminée
        if (detectionFuture != null && !detectionFuture.isDone()) {
            try {
                System.out.println("Waiting for detection to complete...");
                detectionFuture.get(5, TimeUnit.SECONDS); // Timeout de 5 secondes
            } catch (Exception e) {
                System.err.println("Detection task did not complete in time: " + e.getMessage());
                detectionFuture.cancel(true);
            }
        }

        // Sauvegarder la configuration détectée
        if (HardwareScanner != null) {
            HardwareScanner.saveToConfigFile("hardware_config.properties");
        }

        // Arrêter l'ExecutorService proprement
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for ExecutorService termination");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Dans cleanup() avant splash.cleanup() :
        if (splashRenderer != null) {
            splashRenderer.cleanup();
        }

        // Nettoyer la splash window
        if (splash != null) {
            splash.cleanup();
        }

        System.out.println("Cleanup complete");
    }

    // Méthodes utilitaires pour accéder aux informations de détection
    public SystemHardwareScanner getDetectionTask() {
        return HardwareScanner;
    }

    public boolean isDetectionComplete() {
        return HardwareScanner != null && HardwareScanner.isDetectionComplete();
    }
}