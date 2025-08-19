package systeme.filesystem;

import java.io.File;
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

        // Créer le dossier racine du jeu
        createDirectories(gameRoot);

        // === Configuration ===
        createDirectories(gameRoot.resolve("config"));

        // === Sauvegardes ===
        Path savesPath = gameRoot.resolve("saves");
        createDirectories(savesPath);
        createDirectories(savesPath.resolve("worlds"));
        createDirectories(savesPath.resolve("backups"));

        // === Ressources personnalisées uniquement ===
        createDirectories(gameRoot.resolve("texturepacks"));
        createDirectories(gameRoot.resolve("shaderpacks"));

        // === Mods (pour le futur) ===
        Path modsPath = gameRoot.resolve("mods");
        createDirectories(modsPath);
        createDirectories(modsPath.resolve("enabled"));
        createDirectories(modsPath.resolve("disabled"));

        // === Logs ===
        Path logsPath = gameRoot.resolve("logs");
        createDirectories(logsPath);
        createDirectories(logsPath.resolve("crash-reports"));

        // === Utilitaires ===
        createDirectories(gameRoot.resolve("screenshots"));
        createDirectories(gameRoot.resolve("temp"));

        System.out.println("Game directories created at: " + gameRoot);
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

    public File getShaderpacksDirectory() {
        return determineGameDirectory().resolve("shaderpacks").toFile();
    }

    public File getTexturePacksDirectory() {
        return determineGameDirectory().resolve("texturepacks").toFile();
    }

    public File getWorldsDirectory() {
        return determineGameDirectory().resolve("saves/worlds").toFile();
    }

    public File getBackupsDirectory() {
        return determineGameDirectory().resolve("saves/backups").toFile();
    }

    public File getResourcePacksDirectory() {
        return determineGameDirectory().resolve("resourcepacks").toFile();
    }

    public File getModsDirectory() {
        return determineGameDirectory().resolve("mods").toFile();
    }

    public File getLogsDirectory() {
        return determineGameDirectory().resolve("logs").toFile();
    }

    public File getCrashReportsDirectory() {
        return determineGameDirectory().resolve("logs/crash-reports").toFile();
    }

    public File getScreenshotsDirectory() {
        return determineGameDirectory().resolve("screenshots").toFile();
    }

    public File getTempDirectory() {
        return determineGameDirectory().resolve("temp").toFile();
    }

    public File getConfigDirectory() {
        return determineGameDirectory().resolve("config").toFile();
    }
}