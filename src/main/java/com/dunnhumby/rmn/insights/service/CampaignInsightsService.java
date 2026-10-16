package com.dunnhumby.rmn.insights.service;

import com.dunnhumby.rmn.insights.repository.MetricRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Business logic: validates the request, checks the campaign belongs to the tenant, and asks the
 * repository for the unique-customer count.
 */
@Service
public class CampaignInsightsService {

    static final Duration DEFAULT_RANGE = Duration.ofDays(7);
    static final Duration MAX_RANGE = Duration.ofDays(90);
    private static final Pattern ID_FORMAT = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final MetricRepository repository;
    private final Clock clock;

    public CampaignInsightsService(MetricRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * @param tenantId   retailer making the request
     * @param campaignId campaign to query
     * @param metric     clicks, impressions or clickToBasket
     * @param from       inclusive start; defaults to {@code to - 7 days}
     * @param to         exclusive end; defaults to now
     */
    public UniqueCustomerResult uniqueCustomers(String tenantId, String campaignId, Metric metric,
                                                Instant from, Instant to) {
        requireValidId("tenant id", tenantId);
        requireValidId("campaignId", campaignId);

        Instant end = (to != null) ? to : clock.instant();
        Instant start = (from != null) ? from : end.minus(DEFAULT_RANGE);
        if (!start.isBefore(end)) {
            throw new InvalidRequestException("'from' must be before 'to'");
        }
        if (Duration.between(start, end).compareTo(MAX_RANGE) > 0) {
            throw new InvalidRequestException("Time range cannot be longer than " + MAX_RANGE.toDays() + " days");
        }

        if (!repository.campaignExists(tenantId, campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }

        long count = repository.countUniqueCustomers(tenantId, campaignId, metric, start, end);
        return new UniqueCustomerResult(campaignId, metric, start, end, count);
    }

    private static void requireValidId(String name, String value) {
        if (value == null || !ID_FORMAT.matcher(value).matches()) {
            throw new InvalidRequestException(name + " must be 1-64 characters: letters, digits, '-' or '_'");
        }
    }
}
