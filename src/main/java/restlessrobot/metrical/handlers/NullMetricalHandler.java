package restlessrobot.metrical.handlers;

import java.io.IOException;

import restlessrobot.metrical.MetricalEvent;
import restlessrobot.metrical.MetricalHandler;

/**
 * Created by simon on 06/06/14.
 */
public class NullMetricalHandler implements MetricalHandler {
    @Override
    public void event(MetricalEvent event) {

    }

    @Override
    public void finish() {

    }

    @Override
    public void reset() throws IOException {

    }
}
