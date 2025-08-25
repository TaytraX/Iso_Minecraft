package world;
import world.chunk.Chunk;

import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    private static ConcurrentHashMap<ChunkCoord, Chunk> chunks = new ConcurrentHashMap<>();

    public WorldManager() {
        generateChunk();
    }

    private void generateChunk() {
        // Exemple : générer quelques blocs dans ce chunk
        // Les coordonnées sont LOCALES au chunk (0-31 pour un chunk de 32x32x32)
        addChunk(0, 0, 0);
        addChunk(2, 0, 0);
    }

    public ConcurrentHashMap<ChunkCoord, Chunk> getLoadedChunk() {
        return chunks;
    }

    public Chunk getChunk(int localX, int localY, int localZ) {
        return chunks.get(new ChunkCoord(localX, localY, localZ));
    }

    public void addChunk(int x, int y, int z) {
        ChunkCoord coord = new ChunkCoord(x, y, z);
        chunks.put(coord, new Chunk());
    }

    public ChunkCoord getPosition{
        return
    }
}