package world.chunk;

import world.block.Block;
import world.block.Dearth;
import world.block.LocalBlockCoord;

import java.util.HashMap;

public class Chunk extends HashMap<LocalBlockCoord, Block> {
    private final ChunkCoord position;

    public Chunk(ChunkCoord position) {
        this.position = position;
        generateBlocks();
    }

    private void generateBlocks() {
        int chunkSize = 23; // largeur/longueur du chunk

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                for (int y = 0; y < chunkSize; y++) {
                    addBlock(x, y, z); // sol par défaut
                }
            }
        }
    }

    // Ajouter un bloc au chunk (coordonnées locales)
    public void addBlock(int x, int y, int z) {
        LocalBlockCoord pos = new LocalBlockCoord((byte) x, (byte) y, (byte) z);
        put(pos, new Dearth(pos));
    }

    public ChunkCoord getPosition() {
        return position;
    }
}