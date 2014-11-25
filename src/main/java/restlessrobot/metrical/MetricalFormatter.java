package restlessrobot.metrical;

/**
 * Created by simon on 06/06/14.
 */
public interface MetricalFormatter {
    String event(MetricalEvent event);

    void reset();
}
