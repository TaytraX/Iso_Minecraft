package systeme;

import game.Engine;
import systeme.startup.Startup;

public class Teste {
    private static Startup startup;
    private static Engine engine = new Engine();

    static {
        try {
            startup = new Startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        startup.run();
    }
}