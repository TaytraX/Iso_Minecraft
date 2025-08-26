package world.chunk;

public record LocalBlockCoord(Byte x, Byte y, Byte z) {
    public LocalBlockCoord {
        if (x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) {
            throw new IllegalArgumentException("Coordonnées locales hors limites: " + x + "," + y + "," + z);
        }
    }
}