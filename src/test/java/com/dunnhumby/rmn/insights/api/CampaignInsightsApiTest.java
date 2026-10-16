package com.dunnhumby.rmn.insights.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunnhumby.rmn.insights.repository.InMemoryMetricRepository;
import com.dunnhumby.rmn.insights.service.Metric;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "insights.sample-data=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CampaignInsightsApiTest {

    @LocalServerPort
    int port;

    @Autowired
    InMemoryMetricRepository repository;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll
    void seed() {
        Instant t = Instant.parse("2026-10-10T10:00:00Z");
        repository.addCampaign("retailer-a", "cmp-1");
        repository.addCampaign("retailer-b", "cmp-2");
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, t, "c1");
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, t.plusSeconds(5), "c1");
        repository.record("retailer-a", "cmp-1", Metric.CLICKS, t, "c2");
        repository.record("retailer-a", "cmp-1", Metric.CLICK_TO_BASKET, t.plusSeconds(60), "c2");
    }

    private HttpResponse<String> get(String path, String tenant) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        if (tenant != null) {
            request.header("X-Tenant-Id", tenant);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void returnsUniqueClicks() throws Exception {
        HttpResponse<String> response = get("/ad/cmp-1/clicks?from=2026-10-10T00:00:00Z&to=2026-10-11T00:00:00Z", "retailer-a");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat((Integer) JsonPath.read(response.body(), "$.uniqueCustomers")).isEqualTo(2);
        assertThat((String) JsonPath.read(response.body(), "$.metric")).isEqualTo("clicks");
        assertThat((String) JsonPath.read(response.body(), "$.campaignId")).isEqualTo("cmp-1");
    }

    @Test
    void returnsClickToBasketAndImpressions() throws Exception {
        String range = "?from=2026-10-10T00:00:00Z&to=2026-10-11T00:00:00Z";
        HttpResponse<String> basket = get("/ad/cmp-1/clickToBasket" + range, "retailer-a");
        HttpResponse<String> impressions = get("/ad/cmp-1/impressions" + range, "retailer-a");

        assertThat((Integer) JsonPath.read(basket.body(), "$.uniqueCustomers")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(impressions.body(), "$.uniqueCustomers")).isZero();
    }

    @Test
    void missingTenantHeaderIs400() throws Exception {
        HttpResponse<String> response = get("/ad/cmp-1/clicks", null);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("X-Tenant-Id");
    }

    @Test
    void badDateIs400() throws Exception {
        assertThat(get("/ad/cmp-1/clicks?from=yesterday", "retailer-a").statusCode()).isEqualTo(400);
    }

    @Test
    void otherTenantsCampaignIs404() throws Exception {
        assertThat(get("/ad/cmp-2/clicks", "retailer-a").statusCode()).isEqualTo(404);
    }
}
