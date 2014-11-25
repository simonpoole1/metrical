package restlessrobot.metrical.handlers;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by simon on 11/06/14.
 */
public class FileMetricalHandler extends TextOutputMetricalHandler {
    // Construct DateFormat per-thread, because it's not thread-safe
    private static final ThreadLocal<DateFormat> ISO_TIMESTAMP_FORMAT
            = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            df.setTimeZone(tz);
            return df;
        }
    };

    private final String pathStem;
    private final String pathSuffix;

    private Writer currentWriter;
    private String currentPath;

    public FileMetricalHandler(String pathStem, String pathSuffix) {
        this.pathStem = pathStem;
        this.pathSuffix = pathSuffix;
    }

    @Override
    protected void output(String text) throws IOException {
        if (currentWriter == null) {
            startNewFile();
        }
        currentWriter.write(text);
    }

    @Override
    public void finish() {
        try {
            if (currentWriter != null) {
                currentWriter.flush();
            }
            reset();
        } catch (IOException e) {
            // suppress exception.
            e.printStackTrace();
        }
    }

    private Writer startNewFile() throws IOException {
        String isoTimestamp = ISO_TIMESTAMP_FORMAT.get().format(new Date(getTimeProvider().currentTimeMillis()));
        String path = new StringBuilder(pathStem).append(isoTimestamp).append(pathSuffix).toString();

        File f = new File(path);
        Writer writer = new BufferedWriter(new FileWriter(f));

        currentPath           = path;
        currentWriter         = writer;
        return writer;
    }

    public void reset() throws IOException {
        super.reset();
        if (currentWriter != null) {
            currentWriter.close();
            currentWriter = null;
        }
        currentPath = null;
    }

    @VisibleForTesting
    String getCurrentPath() {
        return currentPath;
    }
}
