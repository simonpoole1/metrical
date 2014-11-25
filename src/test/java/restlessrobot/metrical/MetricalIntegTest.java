package restlessrobot.metrical;

import org.junit.Before;
import org.junit.Test;

import restlessrobot.metrical.handlers.TextCaptureMetricalHandler;

import static org.junit.Assert.assertEquals;
import static restlessrobot.metrical.Metrical.d;
import static restlessrobot.metrical.Metrical.m;
import static restlessrobot.metrical.Metrical.c;

/**
 * Created by simon on 09/06/14.
 */
public class MetricalIntegTest {
    public static final long MOCK_TIME = 1_400_000_000_000L;
    private final TextCaptureMetricalHandler handler = new TextCaptureMetricalHandler();
    private final Metrical a = new Metrical(handler);

    @Before
    public void setUp() throws Exception {
        MetricalEvent.setTimeProvider(buildTimeProvider(MOCK_TIME));
    }

    private TimeProvider buildTimeProvider(final long mockTime) {
        return new TimeProvider() {
            @Override
            public long currentTimeMillis() {
                return mockTime;
            }
        };
    }

    @Test
    public void testSimpleEvent() {
        a.event("my-event");
        assertEquals("@v:restlessrobot.metrical:1\n"
                + "@e:1400000000000:my-event:\n", handler.get());
    }

    @Test
    public void testSimpleIntegerEvent() {
        a.event("my-event", m("my-metric", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                + "@e:1400000000000:my-event:\n"
                + "@m:1400000000000:my-event:my-metric:10:ms:\n", handler.get());
    }

    @Test
    public void testSimpleFloatEvent() {
        a.event("my-event", m("my-metric", 3.51471f, Unit.PERCENT));
        assertEquals("@v:restlessrobot.metrical:1\n"
                + "@e:1400000000000:my-event:\n"
                + "@m:1400000000000:my-event:my-metric:3.515:%:\n", handler.get());
    }

    @Test
    public void testMultiMetricEvent() {
        a.event("my-event",
                m("metric1", 10, Unit.MILLISECONDS),
                m("metric2", 3.5f, Unit.PERCENT));
        assertEquals("@v:restlessrobot.metrical:1\n"
                + "@e:1400000000000:my-event:\n"
                + "@m:1400000000000:my-event:metric1:10:ms:\n"
                + "@m:1400000000000:my-event:metric2:3.500:%:\n",
        handler.get());
    }

    @Test
    public void testSingleContextNoDimensions() {
        MetricalContext platformContext = c("platform", true);
        a.addContexts(platformContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                + "@c:platform:g\n"
                + "@e:1400000000000:my-event:platform\n"
                + "@m:1400000000000:my-event:metric1:10:ms:platform\n",
            handler.get());
    }

    @Test
    public void testSingleContextStringDimension() {
        MetricalContext platformContext = c("platform", true,
                d("screen-size", "1024x768"));
        a.addContexts(platformContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@e:1400000000000:my-event:platform\n"
                        + "@m:1400000000000:my-event:metric1:10:ms:platform\n",
                handler.get());
    }

    @Test
    public void testSingleContextIntDimension() {
        MetricalContext platformContext = c("platform", true,
                d("ram", 4, Unit.GIGABYTES));
        a.addContexts(platformContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:ram:4:GB\n"
                        + "@e:1400000000000:my-event:platform\n"
                        + "@m:1400000000000:my-event:metric1:10:ms:platform\n",
                handler.get());
    }

    @Test
    public void testSingleContextFloatDimension() {
        MetricalContext platformContext = c("platform", true,
                d("ram", 2.4f, Unit.GIGABYTES));
        a.addContexts(platformContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:ram:2.400:GB\n"
                        + "@e:1400000000000:my-event:platform\n"
                        + "@m:1400000000000:my-event:metric1:10:ms:platform\n",
                handler.get());
    }


    @Test
    public void testSingleContextWithManyDimensions() {
        MetricalContext platformContext = c("platform", true,
                d("screen-size", "1024x768"),
                d("ram", 1, Unit.GIGABYTES),
                d("os", "Android"),
                d("os-version", "4.4.3"),
                d("my-float", 98.2567f, Unit.NONE));
        a.addContexts(platformContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@d:platform:ram:1:GB\n"
                        + "@d:platform:os:Android:\n"
                        + "@d:platform:os-version:4.4.3:\n"
                        + "@d:platform:my-float:98.26:\n"
                        + "@e:1400000000000:my-event:platform\n"
                        + "@m:1400000000000:my-event:metric1:10:ms:platform\n",
                handler.get());
    }

    @Test
    public void testMultipleContexts() {
        MetricalContext platformContext = c("platform", true,
                d("screen-size", "1024x768"),
                d("ram", 1, Unit.GIGABYTES));
        MetricalContext requestContext = c("request", false,
                d("operation", "get"),
                d("type", "my-type"));
        a.addContexts(platformContext, requestContext);
        a.event("my-event", m("metric1", 10, Unit.MILLISECONDS));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@d:platform:ram:1:GB\n"
                        + "@c:request:\n"
                        + "@d:request:operation:get:\n"
                        + "@d:request:type:my-type:\n"
                        + "@e:1400000000000:my-event:platform,request\n"
                        + "@m:1400000000000:my-event:metric1:10:ms:platform,request\n",
                handler.get());
    }

    @Test
    public void testWithContextsNoOuterContext() {
        MetricalContext requestContext = c("request", false,
                d("operation", "get"),
                d("type", "my-type"));
        Metrical a2 = a.withContexts(requestContext);

        a2.event("event1", m("metric1", 10, Unit.MILLISECONDS));
        a.event("event2", m("metric2", 1, Unit.PERCENT));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:request:\n"
                        + "@d:request:operation:get:\n"
                        + "@d:request:type:my-type:\n"
                        + "@e:1400000000000:event1:request\n"
                        + "@m:1400000000000:event1:metric1:10:ms:request\n"
                        + "@e:1400000000000:event2:\n"
                        + "@m:1400000000000:event2:metric2:1:%:\n",
                handler.get());

    }

    @Test
    public void testWithContextsWithOuterContext() {
        MetricalContext platformContext = c("platform", true,
                d("screen-size", "1024x768"),
                d("ram", 1, Unit.GIGABYTES));
        a.addContexts(platformContext);

        a.event("event1", m("metric1", 1, Unit.PERCENT));

        MetricalContext requestContext = c("request", false,
                d("operation", "get"),
                d("type", "my-type"));
        Metrical a2 = a.withContexts(requestContext);

        a2.event("event2", m("metric2", 10, Unit.MILLISECONDS));
        a.event("event3", m("metric3", 1, Unit.PERCENT));
        assertEquals("@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@d:platform:ram:1:GB\n"
                        + "@e:1400000000000:event1:platform\n"
                        + "@m:1400000000000:event1:metric1:1:%:platform\n"
                        + "@c:request:\n"
                        + "@d:request:operation:get:\n"
                        + "@d:request:type:my-type:\n"
                        + "@e:1400000000000:event2:platform,request\n"
                        + "@m:1400000000000:event2:metric2:10:ms:platform,request\n"
                        + "@e:1400000000000:event3:platform\n"
                        + "@m:1400000000000:event3:metric3:1:%:platform\n",
                handler.get());


    }

    @Test
    public void testWithContextsAndLogRotation() {
        TimeProvider timeProvider = buildTimeProvider(MOCK_TIME);
        MetricalEvent.setTimeProvider(timeProvider);
        handler.setTimeProvider(timeProvider);

        MetricalContext platformContext = c("platform", true,
                d("screen-size", "1024x768"),
                d("ram", 1, Unit.GIGABYTES));
        a.addContexts(platformContext);

        a.event("event1", m("metric1", 1, Unit.PERCENT));

        timeProvider = buildTimeProvider(MOCK_TIME + 1_000);
        MetricalEvent.setTimeProvider(timeProvider);
        handler.setTimeProvider(timeProvider);

        a.event("event1", m("metric1", 5, Unit.PERCENT));

        timeProvider = buildTimeProvider(MOCK_TIME + 600_000);
        MetricalEvent.setTimeProvider(timeProvider);
        handler.setTimeProvider(timeProvider);

        a.event("event1", m("metric1", 2, Unit.PERCENT));

        assertEquals(     "@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@d:platform:ram:1:GB\n"
                        + "@e:1400000000000:event1:platform\n"
                        + "@m:1400000000000:event1:metric1:1:%:platform\n"
                        + "@e:1400000001000:event1:platform\n"
                        + "@m:1400000001000:event1:metric1:5:%:platform\n"
                        + "@v:restlessrobot.metrical:1\n"
                        + "@c:platform:g\n"
                        + "@d:platform:screen-size:1024x768:\n"
                        + "@d:platform:ram:1:GB\n"
                        + "@e:1400000600000:event1:platform\n"
                        + "@m:1400000600000:event1:metric1:2:%:platform\n",
                handler.get());

    }

}
