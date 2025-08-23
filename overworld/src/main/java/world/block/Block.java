package world.block;

import org.joml.Vector3f;
import world.CoordSystem3D;
import world.MeshCube;

public abstract class Block {
    protected final CoordSystem3D position;
    protected final MeshCube block;

    protected Block(Vector3f position) {
        this.position = new CoordSystem3D(new Vector3f(position));
        block = new MeshCube(new Vector3f(this.position.coord()), new Vector3f(1, 1, 1));
    }

    public abstract Vector3f getPosition();

    public abstract MeshCube getBlock();

    public abstract String getTextureName();
}