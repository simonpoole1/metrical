package restlessrobot.metrical;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * Representation of a Metrical event.  Events have a name and a timestamp, and can have zero or
 * more metrics attached.  They may also have a metrics context.
 */
@Data
public class MetricalEvent {
    /*
        TODO: Handle local clock skew.  In some situations the local clock time isn't reliable - e.g.
        if we're running on an Android device where the user has the time set incorrectly.  There's
        not a huge amount we can do about this, but we could periodically contact a reliable time-
        source to calculate an offset?
    */
    private static TimeProvider timeProvider = new TimeProvider() {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    /**
     * @return The name of this event
     */
    private final String name;
    /**
     * @return An immutable list of the metrics attached to this event
     */
    private final List<MetricalMetric<? extends Number>> metrics;
    /**
     * @return An immutable list of the contexts attached to this event
     */
    private final List<MetricalContext> contexts;
    /**
     * @return The time that this event was created (in milliseconds since the epoch)
     */
    private final long timestamp;

    private MetricalEvent(String name, List<MetricalMetric<? extends Number>> metrics,
            List<MetricalContext> contexts) {
        this.name    = name;
        this.metrics = ImmutableList.copyOf(metrics);
        this.contexts  = ImmutableList.copyOf(contexts);
        this.timestamp = timeProvider.currentTimeMillis();
    }

    static MetricalEventBuilder builder() {
        return new MetricalEventBuilder();
    }

    static void setTimeProvider(TimeProvider timeProvider) {
        MetricalEvent.timeProvider = timeProvider;
    }

    static class MetricalEventBuilder {
        private String name;
        private List<MetricalMetric<? extends Number>> metrics = new LinkedList<>();
        private List<MetricalContext> contexts = new LinkedList<>();

        public MetricalEventBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MetricalEventBuilder metric(MetricalMetric metric) {
            if (metric == null) throw new NullPointerException();
            metrics.add(metric);
            return this;
        }

        public MetricalEventBuilder metric(String name, int value, Unit unit) {
            return metric(new MetricalMetric<>(name, value, unit));
        }

        public MetricalEventBuilder metric(String name, float value, Unit unit) {
            return metric(new MetricalMetric<>(name, value, unit));
        }

        public MetricalEventBuilder metric(String name, Number value, Unit unit) {
            return metric(new MetricalMetric<>(name, value, unit));
        }

        public MetricalEventBuilder contexts(Collection<MetricalContext> contexts) {
            if (contexts != null) {
                this.contexts.addAll(contexts);
            }
            return this;
        }

        public MetricalEvent build() {
            return new MetricalEvent(name, metrics, contexts);
        }
    }
}
