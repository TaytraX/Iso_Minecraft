package world.block;

import org.joml.Vector3f;
import world.chunk.LocalBlockCoord;

public record MeshCube(LocalBlockCoord position, Vector3f size) {}