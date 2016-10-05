package org.dsh.metrics;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface Gauge<T> extends Metric {
    T getValue();

    @Override
    default public Map<String,String> getTags() {
        return new HashMap<>();
    }

    default public MetricRegistry getMetricRegistry() {
        return null;
    }
}
