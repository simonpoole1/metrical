package restlessrobot.metrical;

/**
 * Formats a MetricalEvent (and its attached metrics, contexts, dimensions) into a String.
 *
 * The formatting of an individual event may be affected by previous events.  E.g. a formatter may
 * choose not to repeat dimensions that have already been returned.
 */
public interface MetricalFormatter {
    /**
     * Formats a MetricalEvent (and attached metrics, contexts, dimensions) into a String.
     * @param event The event to format
     * @return A string representation of the event
     */
    String event(MetricalEvent event);

    /**
     * Clears any internal state created by previous event() calls.  Subsequent events will be
     * formatted as if a new formatter instance had been created.
     */
    void reset();
}
