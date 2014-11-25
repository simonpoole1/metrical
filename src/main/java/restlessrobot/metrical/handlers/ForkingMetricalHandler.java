package restlessrobot.metrical.handlers;

import java.io.IOException;

import restlessrobot.metrical.MetricalEvent;
import restlessrobot.metrical.MetricalException;
import restlessrobot.metrical.MetricalHandler;

/**
 * Created by simon on 06/06/14.
 */
public class ForkingMetricalHandler implements MetricalHandler {
    private final MetricalHandler[] handlers;

    public ForkingMetricalHandler(MetricalHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void event(MetricalEvent event) throws MetricalException {
        for (MetricalHandler handler : handlers) {
            handler.event(event);
        }
    }

    @Override
    public void finish() {
        for (MetricalHandler handler : handlers) {
            handler.finish();
        }
    }

    @Override
    public void reset() throws IOException {
        for (MetricalHandler handler : handlers) {
            handler.reset();
        }
    }
}
