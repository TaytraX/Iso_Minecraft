package world.block;

import org.joml.Vector3f;
import world.CoordSystem3D;
import world.MeshCube;

public abstract class Block {
    protected final CoordSystem3D position;
    protected final MeshCube block;

    protected Block(CoordSystem3D position) {
        this.position = position;
        block = new MeshCube(new CoordSystem3D(position.coord()), new Vector3f(1, 1, 1));
    }

    public abstract Vector3f getPosition();

    public abstract MeshCube getBlock();

    public abstract String getTextureName();
}