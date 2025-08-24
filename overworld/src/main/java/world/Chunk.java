package world;

import java.util.concurrent.ConcurrentHashMap;

public class Chunk {
    public ConcurrentHashMap<CoordSystem3D ,MeshCube> chunk = new ConcurrentHashMap<>();

    public Chunk() {
        chunk.put(new CoordSystem3D(0,0,0), new MeshCube(new CoordSystem3D(0,0,0), new org.joml.Vector3f(1,1,1)));
    }
}