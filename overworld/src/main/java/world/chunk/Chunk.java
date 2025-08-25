package world.chunk;

import world.ChunkCoord;
import world.block.Block;
import world.block.Dearth;

import java.util.concurrent.ConcurrentHashMap;

public class Chunk {
    public ConcurrentHashMap<LocalBlockCoord, Block> blocks = new ConcurrentHashMap<>();

    public Chunk() {
        generateBlocks();
    }

    private void generateBlocks() {
        // Exemple : générer quelques blocs dans ce chunk
        // Les coordonnées sont LOCALES au chunk (0-31 pour un chunk de 32x32x32)
        addBlock(0, 0, 0);
        addBlock(2, 0, 0);
    }

    // Ajouter un bloc au chunk (coordonnées locales)
    public void addBlock(int x, int y, int z) {
        LocalBlockCoord pos = new LocalBlockCoord((byte) x, (byte) y, (byte) z);
        blocks.put(pos, new Dearth(pos));
    }

    // Récupérer un bloc du chunk (coordonnées locales)
    public Block getBlock(int localX, int localY, int localZ) {
        return blocks.get(new LocalBlockCoord((byte)localX, (byte)localY, (byte)localZ));
    }

    // Getter pour tous les blocs (pour le rendu)
    public ConcurrentHashMap<LocalBlockCoord, Block> getBlocks() {
        return blocks;
    }
}