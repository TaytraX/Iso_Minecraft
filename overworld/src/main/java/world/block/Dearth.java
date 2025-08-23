package world.block;

import org.joml.Vector3f;
import world.CoordSystem3D;
import world.MeshCube;

public class Dearth extends Block {

    private CoordSystem3D position;
    private final MeshCube block;

    public Dearth() {
        super(new CoordSystem3D(new Vector3f(0, 0, 0)));
        block = new MeshCube(position, new Vector3f(1, 1, 1));
    }

    @Override
    public Vector3f getPosition() {
        return position.coord();
    }

    @Override
    public MeshCube getBlock() {
        return block;
    }

    @Override
    public String getTextureName() {
        return "deart";
    }
}