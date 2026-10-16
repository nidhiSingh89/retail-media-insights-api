package com.dunnhumby.rmn.insights.repository;

import com.dunnhumby.rmn.insights.service.Metric;
import java.time.Instant;

/**
 * Read access to the aggregates produced by the stream processor (Flink).
 *
 * <p>Production implementation: Redis HyperLogLog keys per campaign/metric/hour, e.g.
 * {@code rmn:retailer-a:cmp-1001:clicks:2026101608}. {@code PFCOUNT key1 key2 ...} returns the
 * unique count across all hours in the range, without double counting. The in-memory
 * implementation below has the same behaviour and is used for local runs and tests.
 */
public interface MetricRepository {

    /** True if the campaign exists and belongs to the tenant. */
    boolean campaignExists(String tenantId, String campaignId);

    /** Number of distinct customers for the metric with events in {@code [from, to)}. */
    long countUniqueCustomers(String tenantId, String campaignId, Metric metric, Instant from, Instant to);
}
