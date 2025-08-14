package systeme.startup;

public class Startup {
    private SplashWindow splash;
    private DetectionTask detectionTask;
    private SplashRenderer splashRenderer;

    public Startup() {
        splash = new SplashWindow();
        splashRenderer = new SplashRenderer();
    }

    public void init() {
        splash.init();
        //splashRenderer.init();
    }

    public void run() {
        init();
        loop();
        cleanup();

    }

    public void loop() {
        while (!splash.shouldClose()) {
            update();
            render();
        }
    }

    public void render() {
    }

    public void update() {
        splash.update();
    }

    public void cleanup() {
        splash.cleanup();
    }

}