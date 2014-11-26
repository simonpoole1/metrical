package restlessrobot.metrical;

/**
 * Interface for generating MetricalEvent timestamps.  Currently only used for mocking timestamps
 * for testing.
 */
public interface TimeProvider {
    long currentTimeMillis();
}
