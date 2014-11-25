package restlessrobot.metrical.handlers;

/**
 * Created by simon on 09/06/14.
 */
public class TextCaptureMetricalHandler extends TextOutputMetricalHandler {
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void output(String line) {
        sb.append(line);
    }

    public String get() {
        String str = sb.toString();
        sb = new StringBuilder();
        return str;
    }

    @Override
    public void finish() {

    }
}
