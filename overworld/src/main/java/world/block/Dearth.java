package world.block;

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