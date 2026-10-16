package com.dunnhumby.rmn.insights.repository;

import com.dunnhumby.rmn.insights.service.Metric;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads synthetic (randomly generated) data on startup so the API can be tried locally.
 * Disabled in tests with {@code insights.sample-data=false}.
 */
@Component
@ConditionalOnProperty(name = "insights.sample-data", havingValue = "true", matchIfMissing = true)
public class SampleDataLoader implements CommandLineRunner {

    private final InMemoryMetricRepository repository;
    private final Clock clock;

    public SampleDataLoader(InMemoryMetricRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        repository.addCampaign("retailer-a", "cmp-1001");
        repository.addCampaign("retailer-a", "cmp-1002");
        repository.addCampaign("retailer-b", "cmp-2001");

        Random random = new Random(42);
        Instant now = clock.instant();
        for (String campaign : new String[] {"cmp-1001", "cmp-1002"}) {
            for (int i = 0; i < 20_000; i++) {
                String customer = "cust-" + random.nextInt(5_000);
                Instant time = now.minus(Duration.ofMinutes(random.nextInt(14 * 24 * 60)));
                repository.record("retailer-a", campaign, Metric.IMPRESSIONS, time, customer);
                if (random.nextDouble() < 0.05) {
                    repository.record("retailer-a", campaign, Metric.CLICKS, time, customer);
                    if (random.nextDouble() < 0.3) {
                        repository.record("retailer-a", campaign, Metric.CLICK_TO_BASKET, time.plusSeconds(600), customer);
                    }
                }
            }
        }
    }
}
