package world;

import world.chunk.Chunk;
import world.chunk.ChunkCoord;

import java.util.HashMap;

public class WorldManager extends HashMap<ChunkCoord, Chunk> {

    public WorldManager() {
        generateChunk();
    }

    private void generateChunk() {
        // Exemple : générer quelques blocs dans ce chunk
        // Les coordonnées sont LOCALES au chunk (0-31 pour un chunk de 32x32x32)
        addChunk(0, 0, 0);
    }

    public void addChunk(int x, int y, int z) {
        ChunkCoord coord = new ChunkCoord(x, y, z);
        put(coord, new Chunk(coord));
    }
}