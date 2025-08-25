package world;

import java.util.concurrent.ConcurrentHashMap;

public class Chunk {
    public ConcurrentHashMap<CoordSystem3D ,MeshCube> chunks = new ConcurrentHashMap<>();

    public Chunk() {
        chunks.put(new CoordSystem3D(0,0,0), new MeshCube(new CoordSystem3D(1,-25,0), new org.joml.Vector3f(1,1,1)));
        chunks.put(new CoordSystem3D(0,0,0), new MeshCube(new CoordSystem3D(0,0,0), new org.joml.Vector3f(1,1,1)));
    }
}