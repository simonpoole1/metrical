package restlessrobot.metrical;

import java.io.IOException;

/**
 * Implementers of the MetricalHandler interface can be used to process metrics reported via a
 * Metrical instance.
 *
 * The event(...) method is called for each event reported as it occurs (i.e. synchronously).
 */
public interface MetricalHandler {
    /**
     * Called to report a metrics event.  Implementers are expected to return quickly, since this
     * method will be called synchronously by application threads.
     *
     * @param event The event to report
     * @throws MetricalException Thrown if a problem occurs reporting the provided event
     */
    void event(MetricalEvent event) throws MetricalException;

    /**
     * Called to clean up resources after the last event has been reported.  No further events
     * should be expected after finish() has been called, and implementors should flush files,
     * release handles etc.
     */
    void finish();

    /**
     * Called to reset a handler. Implementers should clear any intermediate state and act as if
     * a new handler had been created.
     * @throws IOException
     */
    void reset() throws IOException;
}
