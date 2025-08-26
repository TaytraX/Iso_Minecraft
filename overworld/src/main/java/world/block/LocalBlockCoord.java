package world.block;

public record LocalBlockCoord(Byte x, Byte y, Byte z) {
    static int chunkSize = 24;

    public LocalBlockCoord {
        if (x < 0 || x >= chunkSize - 1 || y < 0 || y >= chunkSize - 1 || z < 0 || z >= chunkSize - 1) {
            throw new IllegalArgumentException(String.format("Coordonnées locales hors limites: %d %d %d", x, y, z));
        }
    }
}