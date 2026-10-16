package com.dunnhumby.rmn.insights.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dunnhumby.rmn.insights.repository.InMemoryMetricRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CampaignInsightsServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private InMemoryMetricRepository repository;
    private CampaignInsightsService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMetricRepository();
        service = new CampaignInsightsService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        repository.addCampaign("retailer-a", "cmp-1");
        repository.addCampaign("retailer-b", "cmp-2");
    }

    @Test
    void countsEachCustomerOnlyOnce() {
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, NOW.minusSeconds(60), "c1");
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, NOW.minusSeconds(30), "c1");
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, NOW.minusSeconds(10), "c2");

        UniqueCustomerResult result = service.uniqueCustomers("retailer-a", "cmp-1", Metric.CLICKS, null, null);

        assertThat(result.uniqueCustomers()).isEqualTo(2);
        assertThat(result.to()).isEqualTo(NOW);
        assertThat(result.from()).isEqualTo(NOW.minus(Duration.ofDays(7)));
    }

    @Test
    void onlyCountsEventsInsideTheTimeRange() {
        Instant from = NOW.minus(Duration.ofDays(1));
        repository.record("retailer-a", "cmp-1", Metric.IMPRESSIONS, from.minusSeconds(1), "before");
        repository.record("retailer-a", "cmp-1", Metric.IMPRESSIONS, from, "inside");
        repository.record("retailer-a", "cmp-1", Metric.IMPRESSIONS, NOW, "atEndExcluded");

        long count = service.uniqueCustomers("retailer-a", "cmp-1", Metric.IMPRESSIONS, from, NOW).uniqueCustomers();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void metricsAreCountedSeparately() {
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, NOW.minusSeconds(60), "c1");
        repository.record("retailer-a", "cmp-1", Metric.CLICK_TO_BASKET, NOW.minusSeconds(30), "c1");

        assertThat(service.uniqueCustomers("retailer-a", "cmp-1", Metric.IMPRESSIONS, null, null).uniqueCustomers()).isZero();
        assertThat(service.uniqueCustomers("retailer-a", "cmp-1", Metric.CLICK_TO_BASKET, null, null).uniqueCustomers()).isEqualTo(1);
    }

    @Test
    void tenantCannotSeeAnotherTenantsCampaign() {
        assertThatThrownBy(() -> service.uniqueCustomers("retailer-a", "cmp-2", Metric.CLICKS, null, null))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void rejectsInvalidRequests() {
        assertThatThrownBy(() -> service.uniqueCustomers("retailer-a", "cmp-1", Metric.CLICKS, NOW, NOW.minusSeconds(1)))
                .isInstanceOf(InvalidRequestException.class).hasMessageContaining("before");
        assertThatThrownBy(() -> service.uniqueCustomers("retailer-a", "cmp-1", Metric.CLICKS, NOW.minus(Duration.ofDays(91)), NOW))
                .isInstanceOf(InvalidRequestException.class).hasMessageContaining("90 days");
        assertThatThrownBy(() -> service.uniqueCustomers("retailer-a", "bad id!", Metric.CLICKS, null, null))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.uniqueCustomers(" ", "cmp-1", Metric.CLICKS, null, null))
                .isInstanceOf(InvalidRequestException.class);
    }
}
