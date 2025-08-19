package systeme.config;

import systeme.filesystem.GameDirectoryManager;
import systeme.startup.SystemHardwareScanner;

public class OptimizedConfigGenerator {
    private final SystemHardwareScanner scanner;
    private final GameDirectoryManager directoryManager;

    public OptimizedConfigGenerator(SystemHardwareScanner scanner) {
        this.scanner = scanner;
        directoryManager = new GameDirectoryManager();
    }
}