package world;

import org.jetbrains.annotations.Contract;
import org.joml.Vector3f;

public record MeshCube(CoordSystem3D position, Vector3f size) {

    @Contract
    public float getMinX() { return position.coord().x - size.x; }

    @Contract
    public float getMaxX() { return position.coord().x + size.x; }

    @Contract
    public float getMinY() { return position.coord().y - size.y; }

    @Contract
    public float getMaxY() { return position.coord().y + size.y; }

    @Contract
    public float getMinZ() { return position.coord().z - size.z; }

    @Contract
    public float getMaxZ() { return position.coord().z + size.z; };
}