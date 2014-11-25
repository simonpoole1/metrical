package restlessrobot.metrical.handlers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import restlessrobot.metrical.MetricalEvent;
import restlessrobot.metrical.MetricalFormatter;
import restlessrobot.metrical.TimeProvider;

import static org.junit.Assert.*;

public class FileMetricalHandlerTest {
    /**
     * Mock time: Tue May 13 16:53:20 2014
     */
    private static final long MOCK_TIME_EPOCH_MILLIS = 1_400_000_000_000L;
    private static final long MOCK_TIME_2_EPOCH_MILLIS = 1_400_000_600_000L;
    private static final long MOCK_TIME_3_EPOCH_MILLIS = 1_400_000_001_000L;
    private static final String MOCK_TIME_ISO = "2014-05-13T16:53:20.000Z";
    private static final String MOCK_TIME_2_ISO = "2014-05-13T17:03:20.000Z";
    private static final String MOCK_TIME_3_ISO = "2014-05-13T16:53:21.000Z";
    public static final String SINGLE_LINE_TEXT_1 = "Some text\n";
    public static final String SINGLE_LINE_TEXT_2 = "Some more text\n";
    public static final String MULTI_LINE_TEXT_1 = "Multiple lines\nof text\n";
    public static final String RESET_TEXT = "RESET\n";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock private MetricalEvent mockEvent1;
    @Mock private MetricalEvent mockEvent2;
    @Mock private MetricalEvent mockEvent3;

    private MetricalFormatter mockFormatter;

    private FileMetricalHandler handler;

    private String pathStem;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockFormatter = new MetricalFormatter() {
            private boolean isReset = false;
            @Override
            public String event(MetricalEvent event) {
                StringBuffer sb = new StringBuffer();
                if (isReset) sb.append(RESET_TEXT);
                if (event == mockEvent1) {
                    sb.append(SINGLE_LINE_TEXT_1);
                } else if (event == mockEvent2) {
                    sb.append(SINGLE_LINE_TEXT_2);
                } else if (event == mockEvent3) {
                    sb.append(MULTI_LINE_TEXT_1);
                }
                isReset = false;
                return sb.toString();
            }

            @Override
            public void reset() {
                isReset = true;

            }
        };


        pathStem = testFolder.getRoot().getCanonicalPath() + File.separator + "metrics-";
        handler = new FileMetricalHandler(pathStem, ".log");
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_EPOCH_MILLIS));
        handler.setFormatter(mockFormatter);
    }

    private String mockText(String text) {
        return text;
    }

    @Test
    public void testOutputFilePath() throws Exception {
        handler.event(mockEvent1);
        String path = handler.getCurrentPath();
        File f = new File(pathStem + MOCK_TIME_ISO + ".log");
        assertEquals("File path", f.getCanonicalPath(), path);
    }

    @Test
    public void testOutputFileCreated() throws Exception {
        handler.event(mockEvent1);
        handler.finish();

        File f = new File(pathStem + MOCK_TIME_ISO + ".log");
        assertTrue("File created", f.exists());
    }

    @Test
    public void testOutputFileContents() throws Exception {
        handler.event(mockEvent1);
        handler.finish();

        File f = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f);
        assertEquals("Expected file contents", RESET_TEXT + SINGLE_LINE_TEXT_1, contents);
    }

    @Test
    public void testOutputFileContentsMultipleCalls() throws Exception {
        handler.event(mockEvent1);
        handler.event(mockEvent2);
        handler.event(mockEvent3);
        handler.finish();

        File f = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f);
        assertEquals("Expected file contents",
                RESET_TEXT + SINGLE_LINE_TEXT_1 + SINGLE_LINE_TEXT_2 + MULTI_LINE_TEXT_1,
                contents);
    }

    @Test
    public void testFileRotationByAge() throws Exception {
        handler.event(mockEvent1);
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_2_EPOCH_MILLIS));
        handler.event(mockEvent2);
        handler.finish();

        File f1 = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f1);
        assertEquals("Expected first file contents", RESET_TEXT + SINGLE_LINE_TEXT_1, contents);

        File f2 = new File(pathStem + MOCK_TIME_2_ISO + ".log");
        contents = readFileContents(f2);
        assertEquals("Expected second file contents", RESET_TEXT + SINGLE_LINE_TEXT_2, contents);
    }

    @Test
    public void testFileRotationByAgeNoRotate() throws Exception {
        // Does not rotate the file, because the time only changes by 1 second
        handler.event(mockEvent1);
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_3_EPOCH_MILLIS));
        handler.event(mockEvent2);
        handler.finish();

        File f1 = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f1);
        assertEquals("Expected first file contents",
                RESET_TEXT + SINGLE_LINE_TEXT_1 +  SINGLE_LINE_TEXT_2, contents);
    }

    @Test
    public void testFileRotationBySizeFirstLineExceedsSize() throws Exception {
        handler.setRotateSizeLimitBytes(5);
        handler.event(mockEvent1);
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_3_EPOCH_MILLIS));
        handler.event(mockEvent2);
        handler.finish();

        File f1 = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f1);
        assertEquals("Expected first file contents", RESET_TEXT + SINGLE_LINE_TEXT_1, contents);

        File f2 = new File(pathStem + MOCK_TIME_3_ISO + ".log");
        contents = readFileContents(f2);
        assertEquals("Expected second file contents", RESET_TEXT + SINGLE_LINE_TEXT_2, contents);
    }

    @Test
    public void testFileRotationBySizeSecondLineExceedsSize() throws Exception {
        handler.setRotateSizeLimitBytes(15);
        handler.event(mockEvent1);
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_3_EPOCH_MILLIS));
        handler.event(mockEvent2);
        handler.finish();

        File f1 = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f1);
        assertEquals("Expected first file contents", RESET_TEXT + SINGLE_LINE_TEXT_1, contents);

        File f2 = new File(pathStem + MOCK_TIME_3_ISO + ".log");
        contents = readFileContents(f2);
        assertEquals("Expected second file contents", RESET_TEXT + SINGLE_LINE_TEXT_2, contents);
    }

    @Test
    public void testFileRotationBySizeSecondLineWithinSize() throws Exception {
        handler.setRotateSizeLimitBytes(31);
        handler.event(mockEvent1);
        handler.setTimeProvider(buildMockTimeProvider(MOCK_TIME_3_EPOCH_MILLIS));
        handler.event(mockEvent2);
        handler.finish();

        File f1 = new File(pathStem + MOCK_TIME_ISO + ".log");
        String contents = readFileContents(f1);
        assertEquals("Expected first file contents",
                RESET_TEXT + SINGLE_LINE_TEXT_1 +  SINGLE_LINE_TEXT_2, contents);
    }


    private String readFileContents(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private TimeProvider buildMockTimeProvider(final long mockTime) {
        TimeProvider timeProvider = new TimeProvider() {
            @Override
            public long currentTimeMillis() {
                return mockTime;
            }
        };
        return timeProvider;
    }

}
