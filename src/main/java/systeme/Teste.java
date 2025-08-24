package systeme;

import game.Engine;
import systeme.startup.Startup;

public class Teste {
    private static final Startup startup;
    private static final Engine engine = new Engine();

    static {
        try {
            startup = new Startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //startup.run();
        engine.start();
    }
}