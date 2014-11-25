package restlessrobot.metrical.formatters;

import com.google.common.base.Joiner;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import restlessrobot.metrical.MetricalMetric;
import restlessrobot.metrical.MetricalDimension;
import restlessrobot.metrical.MetricalEvent;
import restlessrobot.metrical.MetricalFormatter;
import restlessrobot.metrical.MetricalContext;
import restlessrobot.metrical.Unit;

/**
 * Created by simon on 06/06/14.
 */
public class SimpleMetricalFormatter implements MetricalFormatter {
    private static final String LINE_SEPARATOR = "\n";
    private static final String LINE_PREFIX = "@";
    private static final String VERSION_KEY = "v";
    private static final String EVENT_KEY = "e";
    private static final String METRIC_KEY = "m";
    private static final String CONTEXT_KEY = "c";
    private static final String DIMENSION_KEY = "d";
    private static final String CONTEXT_GLOBAL_FLAG = "g";
    private static final int VERSION_NO = 1;
    private static final String VERSION_LINE = LINE_PREFIX + VERSION_KEY + ":restlessrobot.metrical:" + VERSION_NO + LINE_SEPARATOR;
    private static final Joiner colonJoiner = Joiner.on(':').useForNull("");
    private static final Joiner commaJoiner = Joiner.on(',').skipNulls();

    private final Map<String, MetricalContext> contextsAlreadyOutput = new HashMap<>();
    private boolean versionLineDone = false;

    @Override
    public String event(MetricalEvent event) {
        StringBuilder sb = new StringBuilder();

        if (!versionLineDone) {
            sb.append(VERSION_LINE);
            versionLineDone = true;
        }

        List<MetricalContext> contexts = event.getContexts();
        for (MetricalContext context : contexts) {
            if (!context.equals(contextsAlreadyOutput.get(context.getName()))) {
                contextsAlreadyOutput.put(context.getName(), context);
                sb.append(context(context));
            }
        }

        List<MetricalMetric<? extends Number>> metrics = event.getMetrics();

        outputEvent(sb, event, contexts);
        for (MetricalMetric<? extends Number> metric : metrics) {
            outputMetric(sb, event, metric, contexts);
        }
        return sb.toString();
    }

    private void outputEvent(StringBuilder sb, MetricalEvent event, List<MetricalContext> contexts) {
        sb.append(LINE_PREFIX);
        sb.append(colonJoiner.join(
                EVENT_KEY,
                event.getTimestamp(),
                event.getName(),
                joinContextNames(contexts)));
        sb.append(LINE_SEPARATOR);
    }

    private void outputMetric(StringBuilder sb, MetricalEvent event, MetricalMetric<? extends Number> metric, List<MetricalContext> contexts) {
        sb.append(LINE_PREFIX);
        sb.append(colonJoiner.join(
                METRIC_KEY,
                event.getTimestamp(),
                event.getName(),
                metric.getName(),
                formatValue(metric.getValue()),
                metric.getUnit().getShortName(),
                joinContextNames(contexts)));
        sb.append(LINE_SEPARATOR);
    }

    @Override
    public void reset() {
        contextsAlreadyOutput.clear();
        versionLineDone = false;
    }

    private String context(MetricalContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_PREFIX);
        sb.append(colonJoiner.join(
                CONTEXT_KEY,
                context.getName(),
                context.isGlobal() ? CONTEXT_GLOBAL_FLAG : null
                ));
        sb.append(LINE_SEPARATOR);

        Map<String, MetricalDimension> dimensions = context.getDimensions();
        for (MetricalDimension dimension : dimensions.values()) {
            sb.append(LINE_PREFIX);
            sb.append(colonJoiner.join(
                    DIMENSION_KEY,
                    context.getName(),
                    dimension.getName(),
                    formatValue(dimension.getValue()),
                    dimension.getUnit().getShortName()));
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private String formatNumber(int value) {
        return Integer.toString(value);
    }

    private String formatNumber(float value) {
        return String.format("%.4g", value);
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return ((String) value).replace(':', '_');
        } else if (value instanceof Integer) {
            return formatNumber((Integer) value);
        } else if (value instanceof Float) {
            return formatNumber((Float) value);
        } else {
            throw new IllegalArgumentException("Invalid value type: " + value.getClass().getCanonicalName());
        }
    }

    private String joinContextNames(MetricalContext[] contexts) {
        if (contexts == null || contexts.length == 0) {
            return null;
        } else {
            String[] names = new String[contexts.length];
            for (int i = 0; i < contexts.length; i++) {
                names[i] = contexts[i].getName();
            }
            return commaJoiner.join(names);
        }
    }

    private String joinContextNames(Collection<MetricalContext> contexts) {
        if (contexts == null || contexts.size() == 0) {
            return null;
        } else {
            String[] names = new String[contexts.size()];
            int i = 0;
            for (MetricalContext context : contexts) {
                names[i++] = context.getName();
            }
            return commaJoiner.join(names);
        }
    }

}
