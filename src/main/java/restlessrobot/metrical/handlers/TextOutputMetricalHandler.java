package restlessrobot.metrical.handlers;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;

import restlessrobot.metrical.MetricalEvent;
import restlessrobot.metrical.MetricalException;
import restlessrobot.metrical.MetricalFormatter;
import restlessrobot.metrical.MetricalHandler;
import restlessrobot.metrical.TimeProvider;
import restlessrobot.metrical.formatters.SimpleMetricalFormatter;

/**
 * Created by simon on 06/06/14.
 */
public abstract class TextOutputMetricalHandler implements MetricalHandler {
    /*
    TODO: Handle local clock skew.  In some situations the local clock time isn't reliable - e.g.
    if we're running on an Android device where the user has the time set incorrectly.  There's
    not a huge amount we can do about this, but we could periodically contact a reliable time-
    source to calculate an offset?
*/
    private TimeProvider timeProvider = new TimeProvider() {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    public static final int DEFAULT_SIZE_LIMIT_BYTES = 50 * 1024;
    public static final int DEFAULT_TIME_LIMIT_MILLIS = 5 * 60 * 1000;

    private MetricalFormatter formatter = new SimpleMetricalFormatter();

    private long rotateSizeLimitBytes  = DEFAULT_SIZE_LIMIT_BYTES; // 50 kB
    private long rotateTimeLimitMillis = DEFAULT_TIME_LIMIT_MILLIS; // 5 mins
    private long charsSinceLastReset;
    private long lastResetTime;

    @Override
    public void event(MetricalEvent event) throws MetricalException {
        try {
            if (lastResetTime == 0) {
                reset();
            } else if (lastResetTime != 0) {
                long now = timeProvider.currentTimeMillis();
                if (now - lastResetTime > rotateTimeLimitMillis) {
                    reset();
                }
            }

            String text = formatter.event(event);
            int length = text.length();
            if (charsSinceLastReset > 0 && charsSinceLastReset + length > rotateSizeLimitBytes) {
                reset();

                text = formatter.event(event);
                length = text.length();
            }

            output(text);
            charsSinceLastReset += length;
            if (lastResetTime == 0)
                lastResetTime = timeProvider.currentTimeMillis();

        } catch (IOException e) {
            throw new MetricalException("Failed to record event", e);
        }
    }

    @Override
    public void reset() throws IOException {
        charsSinceLastReset = 0;
        lastResetTime = 0;
        formatter.reset();
    }

    protected abstract void output(String line) throws IOException;

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }


    public void setRotateSizeLimitBytes(long rotateSizeLimitBytes) {
        this.rotateSizeLimitBytes = rotateSizeLimitBytes;
    }

    public void setRotateTimeLimitMillis(long rotateTimeLimitMillis) {
        this.rotateTimeLimitMillis = rotateTimeLimitMillis;
    }

    @VisibleForTesting
    void setFormatter(MetricalFormatter formatter) {
        this.formatter = formatter;
    }
}
