package systeme.filesystem;

import systeme.startup.SystemHardwareScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameDirectoryManager {

    private static Path determineGameDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        return switch (os) {
            case String s when s.contains("win") ->
                    Paths.get(System.getenv("APPDATA"), ".iso_minecraft");
            case String s when s.contains("mac") ->
                    Paths.get(userHome, "Library", "Application Support", "iso_minecraft");
            default ->
                    Paths.get(userHome, ".iso_minecraft");
        };
    }

    public void createGameDirectories() {
        Path gameRoot = determineGameDirectory();

        createDirectories(gameRoot);

        createDirectories(gameRoot.resolve("saves"));
        createDirectories(gameRoot.resolve("shaderpacks"));
        createDirectories(gameRoot.resolve("texturepacks"));
        createDirectories(gameRoot.resolve("mods"));
        createDirectories(gameRoot.resolve("config"));
    }

    public void createDirectories(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void getSavesDirectory() {

    }

    public void getShaderpacksDirectory() {

    }

    public void getTexturePacksDirectory() {

    }

    public void getModsDirectory() {

    }

    public void getConfigDirectory() {

    }
}