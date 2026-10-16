package com.dunnhumby.rmn.insights.service;

/** The three metrics exposed by the API. Each one counts unique customers, not events. */
public enum Metric {
    /** Customers who clicked an ad of the campaign. */
    CLICKS("clicks"),
    /** Customers who saw an ad of the campaign. */
    IMPRESSIONS("impressions"),
    /** Customers who added a promoted product to their cart after clicking (attributed upstream by Flink). */
    CLICK_TO_BASKET("clickToBasket");

    private final String apiName;

    Metric(String apiName) {
        this.apiName = apiName;
    }

    public String apiName() {
        return apiName;
    }
}
