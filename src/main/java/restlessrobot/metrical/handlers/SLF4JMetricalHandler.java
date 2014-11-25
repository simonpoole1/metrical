package restlessrobot.metrical.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by simon on 06/06/14.
 */
public class SLF4JMetricalHandler extends TextOutputMetricalHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextOutputMetricalHandler.class);

    @Override
    protected void output(String line) {
        LOGGER.info(line);
    }

    @Override
    public void finish() {

    }
}
