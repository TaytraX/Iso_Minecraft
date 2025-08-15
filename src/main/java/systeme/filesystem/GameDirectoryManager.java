package systeme.filesystem;

import systeme.startup.SystemHardwareScanner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameDirectoryManager {

    private Path determineGameDirectory(SystemHardwareScanner scanner) {
        scanner = new SystemHardwareScanner(null);
        String os = scanner.getOsInfo().split(" ")[0].toLowerCase();
        String userHome = System.getProperty("user.home");

        return switch (os) {
            case "windows" ->
                    Paths.get(System.getenv("APPDATA"), ".iso_minecraft");
            case "macos" ->
                    Paths.get(userHome, "Library", "Application Support", "iso_minecraft");
            default ->
                    Paths.get(userHome, ".iso_minecraft");
        };
    }

    public File createGameDirectories(SystemHardwareScanner scanner) {
        File gameDirectoryFile = determineGameDirectory(scanner).toFile();

        if (!gameDirectoryFile.exists()) {
            try {
                gameDirectoryFile.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return gameDirectoryFile;
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