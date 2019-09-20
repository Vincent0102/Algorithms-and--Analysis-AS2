package ship;

/**
 * Submarine.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class Submarine implements Ship{

    @Override
    public String name() {
        return "Submarine";
    }

    @Override
    public int len() {
        return 3;
    }

    @Override
    public int width() {
        return 1;
    }
} // end of class Submarine
