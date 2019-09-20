package ship;

/**
 * Frigate.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class Frigate implements Ship{

    @Override
    public String name() {
        return "Frigate";
    }

    @Override
    public int len() {
        return 4;
    }

    @Override
    public int width() {
        return 1;
    }
} // end of class Frigate
