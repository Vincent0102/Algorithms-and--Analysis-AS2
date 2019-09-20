package ship;

/**
 * Aircraft carrier.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class AircraftCarrier implements Ship{

    @Override
    public String name() {
        return "AircraftCarrier";
    }

    @Override
    public int len() {
        return 3;
    }

    @Override
    public int width() {
        return 2;
    }
} // end of class AircraftCarrier
