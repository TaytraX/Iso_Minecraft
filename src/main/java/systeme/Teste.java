package systeme;

import systeme.exception.ShaderCompilationException;
import systeme.startup.Startup;

public class Teste {
    private static Startup startup;

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