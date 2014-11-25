package restlessrobot.metrical;

/**
 * Created by simon on 06/06/14.
 */
public enum Unit {
    NONE(""),
    PERCENT("%"),
    SECONDS("s", true),
    MILLISECONDS("ms", true),
    PER_SECOND("/s"),
    BYTES("B"),
    KILOBYTES("kB"),
    MEGABYTES("MB"),
    GIGABYTES("GB");

    private final String shortName;
    private final boolean isTimeUnit;

    private Unit(String shortName) {
        this(shortName, false);
    }
    private Unit(String shortName, boolean isTimeUnit) {
        this.shortName = shortName;
        this.isTimeUnit = isTimeUnit;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isTimeUnit() {
        return isTimeUnit;
    }

}
