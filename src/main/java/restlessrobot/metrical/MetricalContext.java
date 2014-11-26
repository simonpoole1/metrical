package restlessrobot.metrical;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * Object representing a Metrical metrics context.
 *
 * A metrics context has a name, which is used as an identifier and should have a consistent meaning
 * throughout your application.  The name can be any arbitrary string.
 *
 * A metrics context usually has one or more attached dimensions which describe the context.  A
 * context without dimensions is currently pointless.
 *
 * This class cannot be instantiated directly, but should instead be created using the
 * Metrical.c(...) method.
 */
@Data
public class MetricalContext {
    /**
     * @return The name of this metrics context
     */
    private final String name;
    /**
     * @return Immutable map of the dimensions attached to this context, indexed by name
     */
    private final Map<String, MetricalDimension> dimensions;

    /**
     * Creates a MetricalContext instance with the given name and dimensions
     *
     * @param name
     * @param dimensions
     */
    MetricalContext(String name, Map<String, MetricalDimension> dimensions) {
        this.name = name;
        this.dimensions = ImmutableMap.copyOf(dimensions);
    }

    static MetricalContextBuilder builder() {
        return new MetricalContextBuilder();
    }

    static class MetricalContextBuilder {
        private String name;
        private Map<String, MetricalDimension> dimensions = new LinkedHashMap<>();

        MetricalContextBuilder name(String name) {
            this.name = name;
            return this;
        }

        MetricalContextBuilder dimensions(MetricalDimension... dimensions) {
            if (dimensions != null) {
                for (MetricalDimension dimension : dimensions) {
                    if (dimension != null)
                        this.dimensions.put(dimension.getName(), dimension);
                }
            }
            return this;
        }

        MetricalContextBuilder dimension(String name, String value) {
            dimensions.put(name, new MetricalDimension(name, value));
            return this;
        }

        MetricalContext build() {
            return new MetricalContext(name, dimensions);
        }
    }

}
