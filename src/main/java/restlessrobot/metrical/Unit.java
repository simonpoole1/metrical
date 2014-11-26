package restlessrobot.metrical;

/**
 * Representations of various units for use in Metrical.
 *
 * TODO: Switch to something like https://java.net/projects/unitsofmeasurement/
 * Ideally we'd use some off-the-shelf units library, but they're all way more heavy-weight than
 * we need, and there are no ratified standards yet.
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
