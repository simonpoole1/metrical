package restlessrobot.metrical;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * Created by simon on 06/06/14.
 */
@Data
public class MetricalContext {
    private final String name;
    private final boolean isGlobal;
    private final Map<String, MetricalDimension> dimensions;

    public MetricalContext(String name, boolean isGlobal,
            Map<String, MetricalDimension> dimensions) {
        this.name = name;
        this.isGlobal = isGlobal;
        this.dimensions = ImmutableMap.copyOf(dimensions);
    }

    public static MetricalContextBuilder builder() {
        return new MetricalContextBuilder();
    }

    public static class MetricalContextBuilder {
        private String name;
        private boolean isGlobal;
        private Map<String, MetricalDimension> dimensions = new LinkedHashMap<>();

        public MetricalContextBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MetricalContextBuilder isGlobal(boolean isGlobal) {
            this.isGlobal = isGlobal;
            return this;
        }

        public MetricalContextBuilder dimensions(MetricalDimension... dimensions) {
            if (dimensions != null) {
                for (MetricalDimension dimension : dimensions) {
                    if (dimension != null)
                        this.dimensions.put(dimension.getName(), dimension);
                }
            }
            return this;
        }

        public MetricalContextBuilder dimension(String name, String value) {
            dimensions.put(name, new MetricalDimension(name, value));
            return this;
        }

        public MetricalContextBuilder dimension(String name, int value, Unit unit) {
            dimensions.put(name, new MetricalDimension(name, value, unit));
            return this;
        }

        public MetricalContextBuilder dimension(String name, float value, Unit unit) {
            dimensions.put(name, new MetricalDimension(name, value, unit));
            return this;
        }

        public MetricalContext build() {
            return new MetricalContext(name, isGlobal, dimensions);
        }
    }

}
