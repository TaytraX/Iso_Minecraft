package world.block;

import org.joml.Vector3f;
import world.MeshCube;

public class Dearth extends Block {

    private Vector3f position;
    private final MeshCube block;

    public Dearth() {
        super(new Vector3f(0,0,0));
        block = new MeshCube(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    }

    @Override
    public Vector3f getPosition() {
        return null;
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