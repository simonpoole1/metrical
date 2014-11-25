package restlessrobot.metrical;

/**
 * Created by simon on 09/06/14.
 */
public class MetricalDimension {
    private final String name;
    private final Object value;
    private final Unit unit;

    public MetricalDimension(String name, Number value, Unit unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public MetricalDimension(String name, String value) {
        this.name = name;
        this.value = value;
        this.unit = Unit.NONE;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }
}
