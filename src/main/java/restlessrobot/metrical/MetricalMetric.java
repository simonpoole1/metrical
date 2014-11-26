package restlessrobot.metrical;

/**
 * Representation of a Metrical metric.  Metrics have a name, a numeric value and a unit.
 *
 * This class cannot be instantiated directly, but should instead be created using the
 * Metrical.m(...) method.
 */
public class MetricalMetric<T extends Number> {
    private final String name;
    private final T value;
    private final Unit unit;

    MetricalMetric(String name, T value, Unit unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    /**
     * @return The name of this metric
     */
    public String getName() {
        return name;
    }

    /**
     * @return The value of this metric
     */
    public T getValue() {
        return value;
    }

    /**
     * @return The unit of this metric
     */
    public Unit getUnit() {
        return unit;
    }
}
