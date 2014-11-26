package restlessrobot.metrical;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The main entry point to the Metrical API - an object that provides metrics-reporting
 * functionality.
 */
public class Metrical {
    private final MetricalHandler handler;
    private boolean isEnabled = true;
    private Map<String, MetricalContext> contexts;

    /**
     * Creates a new Metrical instance that reports metrics to the given handler.
     *
     * @param handler The handler that metrics events should be reported to. Typically used to log
     *                metrics or report metrics to an analytics system.
     */
    public Metrical(MetricalHandler handler) {
        this.handler = handler;
        this.contexts = null;
    }

    /**
     * Creates a new Metrical instance that reports metrics to the given handler using the given
     * contexts.  All metrics events processed by this instance will have the given contexts
     * attached.
     *
     * @param handler The handler that metrics events should be reported to. Typically used to log
     *                metrics or report metrics to an analytics system.
     * @param contexts The contexts to attach to all events processed by this instance
     */
    public Metrical(MetricalHandler handler, MetricalContext... contexts) {
        this.handler = handler;
        addContexts(contexts);
    }

    /**
     * Creates a new Metrical instance derived from an existing instance, with optional additional
     * contexts.  The new instance will re-use the handler and contexts from the existing instance.
     * If any of the additional contexts provided conflict with the previous contexts, then the
     * precedence rules described in withContexts apply.
     *
     * @param other An existing Metrical instance to derive this instance from.
     * @param additionalContexts The additional contexts to attach to all events processed by this
     *                           instance (i.e. in addition to the ones copied from the existing
     *                           Metrical instance).
     */
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

    /**
     * Creates a new Metrical instance derived from an existing instance, with optional additional
     * contexts.  This is essentially a short form of "newInstance = new Metrical(instance, context)",
     * which can be abbreviated to "newInstance = instance.withContexts(context)".
     *
     * @param contexts The additional contexts to attach to all events processed by the new instance
     *                           instance (i.e. in addition to the ones copied from the existing
     *                           Metrical instance).
     */
    public Metrical withContexts(MetricalContext... contexts) {
        if (contexts == null)
            throw new IllegalArgumentException("No contexts provided");

        return new Metrical(this, contexts);
    }

/*    public void event(String name, Collection<MetricalMetric> metrics) {
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
*/

    /**
     * Reports a metrics event and associated metrics to the configured handler, unless this
     * instance is disabled.
     *
     * @param name The name of the event to report
     * @param metrics The metrics to attach to the event
     */
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

    /**
     * Prevents this instance from reporting events to the configured handler.  When this instance
     * is disabled, the event() method calls will return immediately and the events are discarded.
     * Does nothing if this instance is already disabled.
     */
    public void disable() {
        isEnabled = false;
    }

    /**
     * Tells this instance to report future events to the configured handler if it has previously
     * been disabled with disable().  Does nothing if this instance is already enabled.
     */
    public void enable() {
        isEnabled = true;
    }

    /**
     * Flushes any buffers and cleans up any consumed resources.
     */
    public void finish() {
        if (handler == null)
            return;

        handler.finish();
    }

    /**
     * Convenience method that constructs a MetricalMetric object.
     *
     * @param name The name of the metric to create
     * @param value The numeric value of the metric
     * @param unit The units of the metric
     * @return The created MetricalMetric object
     */
    public static MetricalMetric m(String name, int value, Unit unit) {
        return new MetricalMetric(name, value, unit);
    }

    /**
     * Convenience method that constructs a MetricalMetric object.
     *
     * @param name The name of the metric to create
     * @param value The numeric value of the metric
     * @param unit The units of the metric
     * @return The created MetricalMetric object
     */
    public static MetricalMetric m(String name, float value, Unit unit) {
        return new MetricalMetric(name, value, unit);
    }

    /**
     * Convenience method that constructs a MetricalDimension object.
     *
     * @param name The name of the dimension to create
     * @param value The string value of the dimension
     * @return The created MetricalDimension object
     */
    public static MetricalDimension d(String name, String value) {
        return new MetricalDimension(name, value);
    }

    /**
     * Convenience method that constructs a MetricalContext object.
     *
     * @param name The name of the context to create
     * @param dimensions Optional dimensions to attach to this context
     * @return
     */
    public static MetricalContext c(String name, MetricalDimension... dimensions) {
        return MetricalContext.builder().name(name).dimensions(dimensions).build();
    }

    private void addContexts(MetricalContext... contexts) {
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

    private void addContexts(Collection<MetricalContext> contexts) {
        if (contexts == null || contexts.isEmpty())
            throw new IllegalArgumentException("No contexts provided");

        // Use LinkedHashMap to provide predictable order for unit tests
        if (this.contexts == null)
            this.contexts = new LinkedHashMap<>();

        for (MetricalContext context : contexts) {
            this.contexts.put(context.getName(), context);
        }
    }

    private void event(MetricalEvent event) {
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

}
