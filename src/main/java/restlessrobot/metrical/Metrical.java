package restlessrobot.metrical;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by simon on 06/06/14.
 */
public class Metrical {
    private final MetricalHandler handler;
    private boolean isEnabled = true;

    private Map<String, MetricalContext> contexts;

    public Metrical(MetricalHandler handler) {
        this.handler = handler;
        this.contexts = null;
    }

    public Metrical(MetricalHandler handler, MetricalContext... contexts) {
        this.handler = handler;
        addContexts(contexts);
    }

    private Metrical(Metrical other, MetricalContext[] additionalContexts) {
        if (additionalContexts == null)
            throw new IllegalArgumentException("No context arguments provided");

        this.handler   = other.handler;
        this.isEnabled = other.isEnabled;
        if (other.contexts != null) {
            addContexts(other.contexts.values());
        }
        addContexts(additionalContexts);
    }

    public Metrical withContexts(MetricalContext... contexts) {
        if (contexts == null)
            throw new IllegalArgumentException("No contexts provided");

        return new Metrical(this, contexts);
    }

    public void addContexts(MetricalContext... contexts) {
        if (contexts == null)
            throw new IllegalArgumentException("No contexts provided");

        // Use LinkedHashMap to provide predictable order for unit tests
        if (this.contexts == null)
            this.contexts = new LinkedHashMap<>();

        for (MetricalContext context : contexts) {
            if (context != null)
                this.contexts.put(context.getName(), context);
        }
    }

    public void addContexts(Collection<MetricalContext> contexts) {
        if (contexts == null || contexts.isEmpty())
            throw new IllegalArgumentException("No contexts provided");

        // Use LinkedHashMap to provide predictable order for unit tests
        if (this.contexts == null)
            this.contexts = new LinkedHashMap<>();

        for (MetricalContext context : contexts) {
            this.contexts.put(context.getName(), context);
        }
    }


    public void removeContexts(MetricalContext... contexts) {
        if (this.contexts != null) {
            for (MetricalContext context : contexts) {
                if (context != null)
                    this.contexts.remove(context.getName());
            }
        }
    }

    public void event(MetricalEvent event) {
        if (!isEnabled || handler == null)
            return;

        try {
            handler.event(event);
        } catch (MetricalException e) {
            // Catch and suppress the exception
            // TODO: we should count these!
            e.printStackTrace();
        }
    }

    public void event(String name, Collection<MetricalMetric> metrics) {
        if (!isEnabled || handler == null)
            return;

        MetricalEvent.MetricalEventBuilder builder = MetricalEvent.builder();
        builder.name(name);
        for (MetricalMetric metric : metrics) {
            builder.metric(metric);
        }
        if (this.contexts != null)
            builder.contexts(contexts.values());

        MetricalEvent event = builder.build();
        event(event);
    }

    public void event(String name, MetricalMetric... metrics) {
        if (!isEnabled || handler == null)
            return;

        MetricalEvent.MetricalEventBuilder builder = MetricalEvent.builder();
        builder.name(name);
        if (metrics != null) {
            for (MetricalMetric metric : metrics) {
                if (metric != null)
                    builder.metric(metric);
            }
        }
        if (this.contexts != null)
            builder.contexts(contexts.values());

        MetricalEvent event = builder.build();
        event(event);
    }

    public void disable() {
        isEnabled = false;
    }

    public void enable() {
        isEnabled = true;
    }

    public void finish() {
        if (handler == null)
            return;

        handler.finish();
    }

    public static MetricalMetric m(String name, int value, Unit unit) {
        return new MetricalMetric(name, value, unit);
    }

    public static MetricalMetric m(String name, float value, Unit unit) {
        return new MetricalMetric(name, value, unit);
    }

    public static MetricalDimension d(String name, int value, Unit unit) {
        return new MetricalDimension(name, value, unit);
    }

    public static MetricalDimension d(String name, float value, Unit unit) {
        return new MetricalDimension(name, value, unit);
    }

    public static MetricalDimension d(String name, String value) {
        return new MetricalDimension(name, value);
    }

    public static MetricalContext c(String name, boolean isGlobal, MetricalDimension... dimensions) {
        return MetricalContext.builder().name(name).isGlobal(isGlobal).dimensions(dimensions).build();
    }

}
