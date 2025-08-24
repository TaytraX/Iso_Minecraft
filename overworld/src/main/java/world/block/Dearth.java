package world.block;

import world.CoordSystem3D;

public class Dearth extends Block {
    private final CoordSystem3D position;

    public Dearth(CoordSystem3D position) {
        super(position);
        this.position = position;
    }

    @Override
    public CoordSystem3D getPosition() {
        return position;
    }

    @Override
    public String getTextureName() {
        return "deart";
    }
}