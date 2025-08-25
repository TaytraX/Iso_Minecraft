package world.block;

import world.chunk.LocalBlockCoord;

public class Dearth extends Block {
    private final LocalBlockCoord position;

    public Dearth(LocalBlockCoord position) {
        super(position);
        this.position = position;
    }

    @Override
    public LocalBlockCoord getPosition() {
        return position;
    }

    @Override
    public String getTextureName() {
        return "deart";
    }
}