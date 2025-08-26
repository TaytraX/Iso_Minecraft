package world.block;

import org.joml.Vector3f;

public record MeshCube(LocalBlockCoord position, Vector3f size) {

    public float getMinX() { return position.x() - size.x; }
    public float getMaxX() { return position.x() + size.x; }
    public float getMinY() { return position.y() - size.y; }
    public float getMaxY() { return position.y() + size.y; }
    public float getMinZ() { return position.z() - size.z; }
    public float getMaxZ() { return position.z() + size.z; }
}