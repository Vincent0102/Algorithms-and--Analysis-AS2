package ship;

/**
 * Patrol Craft.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class PatrolCraft implements Ship{

    @Override
    public String name() {
        return "PatrolCraft";
    }

    @Override
    public int len() {
        return 2;
    }

    @Override
    public int width() {
        return 1;
    }
} // end of class PatrolCraft
