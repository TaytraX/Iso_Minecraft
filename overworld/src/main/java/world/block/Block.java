package world.block;

import org.joml.Vector3f;
import world.CoordSystem3D;
import world.MeshCube;

public abstract class Block {
    protected final MeshCube block;
    protected final CoordSystem3D position;

    protected Block(CoordSystem3D position) {
        this.position = position;
        block = new MeshCube(position, new Vector3f(1, 1, 1));
    }

    public abstract CoordSystem3D getPosition() ;

    public MeshCube getBlock() { return block; };

    public abstract String getTextureName();
}