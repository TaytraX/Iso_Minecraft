package world.block;

import org.joml.Vector3f;

public abstract class Block {
    protected final MeshCube block;
    protected final LocalBlockCoord position;

    protected Block(LocalBlockCoord position) {
        this.position = position;
        block = new MeshCube(position, new Vector3f(1, 1, 1));
    }

    public abstract LocalBlockCoord getPosition() ;

    public MeshCube getBlock() { return block; };

    public abstract String getTextureName();
}