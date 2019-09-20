package ship;

/**
 * Cruiser.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class Cruiser implements Ship{

    @Override
    public String name() {
        return "Cruiser";
    }

    @Override
    public int len() {
        return 2;
    }

    @Override
    public int width() {
        return 2;
    }
} // end of class Cruiser
