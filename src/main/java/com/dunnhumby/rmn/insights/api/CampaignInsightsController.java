package com.dunnhumby.rmn.insights.api;

import com.dunnhumby.rmn.insights.service.CampaignInsightsService;
import com.dunnhumby.rmn.insights.service.Metric;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints. The tenant comes from the {@code X-Tenant-Id} header. In production the API
 * gateway sets this header from the caller's authenticated token, so clients cannot choose it.
 *
 * <p>Optional query parameters: {@code from}, {@code to} (ISO-8601, e.g. 2026-10-01T00:00:00Z).
 */
@RestController
@RequestMapping("/ad/{campaignId}")
public class CampaignInsightsController {

    static final String TENANT_HEADER = "X-Tenant-Id";

    private final CampaignInsightsService service;

    public CampaignInsightsController(CampaignInsightsService service) {
        this.service = service;
    }

    /** Unique customers who clicked an ad of the campaign. */
    @GetMapping("/clicks")
    public UniqueCustomerResponse clicks(@RequestHeader(TENANT_HEADER) String tenantId,
                                         @PathVariable String campaignId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return get(tenantId, campaignId, Metric.CLICKS, from, to);
    }

    /** Unique customers who saw an ad of the campaign. */
    @GetMapping("/impressions")
    public UniqueCustomerResponse impressions(@RequestHeader(TENANT_HEADER) String tenantId,
                                              @PathVariable String campaignId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return get(tenantId, campaignId, Metric.IMPRESSIONS, from, to);
    }

    /** Unique customers who added a promoted product to their cart after clicking an ad. */
    @GetMapping("/clickToBasket")
    public UniqueCustomerResponse clickToBasket(@RequestHeader(TENANT_HEADER) String tenantId,
                                                @PathVariable String campaignId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return get(tenantId, campaignId, Metric.CLICK_TO_BASKET, from, to);
    }

    private UniqueCustomerResponse get(String tenantId, String campaignId, Metric metric, Instant from, Instant to) {
        return UniqueCustomerResponse.from(service.uniqueCustomers(tenantId, campaignId, metric, from, to));
    }
}
