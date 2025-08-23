package world;

import org.jetbrains.annotations.Contract;
import org.joml.Vector3f;

public record MeshCube(Vector3f position, Vector3f size) {

    @Contract
    public float getMinX() { return position.x - size.x; }

    @Contract
    public float getMaxX() { return position.x + size.x; }

    @Contract
    public float getMinY() { return position.y - size.y; }

    @Contract
    public float getMaxY() { return position.y + size.y; }

    @Contract
    public float getMinZ() { return position.z - size.z; }

    @Contract
    public float getMaxZ() { return position.z + size.z; };
}