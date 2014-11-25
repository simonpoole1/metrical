package restlessrobot.metrical;

/**
 * Created by simon on 09/06/14.
 */
public class MetricalMetric<T extends Number> {
    private final String name;
    private final T value;
    private final Unit unit;

    public MetricalMetric(String name, T value, Unit unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }
}
