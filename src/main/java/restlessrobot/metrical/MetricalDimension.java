package restlessrobot.metrical;

/**
 * Representation of a metrics dimension.  A dimension has a name and a value.  The value is a
 * String.
 *
 * This class cannot be instantiated directly, but should instead be created using the
 * Metrical.d(...) method.
 */
public class MetricalDimension {
    /**
     * @return The name of this dimension
     */
    private final String name;
    /**
     * @return The value of this dimension
     */
    private final Object value;

    MetricalDimension(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
