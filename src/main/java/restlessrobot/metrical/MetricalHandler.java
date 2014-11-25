package restlessrobot.metrical;

import java.io.IOException;

/**
 * Created by simon on 06/06/14.
 */
public interface MetricalHandler {
    void event(MetricalEvent event) throws MetricalException;

    void finish();

    void reset() throws IOException;
}
