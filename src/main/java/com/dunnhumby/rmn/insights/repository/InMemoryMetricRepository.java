package com.dunnhumby.rmn.insights.repository;

import com.dunnhumby.rmn.insights.service.Metric;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory implementation. Stores (time, customer) pairs per tenant/campaign/metric and
 * counts distinct customers in the requested range. Exact, simple, good for local runs and tests.
 */
@Repository
public class InMemoryMetricRepository implements MetricRepository {

    private record Key(String tenantId, String campaignId, Metric metric) {
    }

    private record Entry(Instant time, String customerId) {
    }

    private final Set<String> campaigns = ConcurrentHashMap.newKeySet();
    private final Map<Key, List<Entry>> entries = new ConcurrentHashMap<>();

    public void addCampaign(String tenantId, String campaignId) {
        campaigns.add(tenantId + "/" + campaignId);
    }

    /** Records that a customer did {@code metric} for the campaign at {@code time} (written by Flink in production). */
    public void record(String tenantId, String campaignId, Metric metric, Instant time, String customerId) {
        entries.computeIfAbsent(new Key(tenantId, campaignId, metric), k -> new CopyOnWriteArrayList<>())
                .add(new Entry(time, customerId));
    }

    @Override
    public boolean campaignExists(String tenantId, String campaignId) {
        return campaigns.contains(tenantId + "/" + campaignId);
    }

    @Override
    public long countUniqueCustomers(String tenantId, String campaignId, Metric metric, Instant from, Instant to) {
        return entries.getOrDefault(new Key(tenantId, campaignId, metric), List.of()).stream()
                .filter(e -> !e.time().isBefore(from) && e.time().isBefore(to))
                .map(Entry::customerId)
                .distinct()
                .count();
    }
}
