package com.dunnhumby.rmn.insights.service;

import java.time.Instant;

/** Result of a query: how many unique customers did the metric in [from, to). */
public record UniqueCustomerResult(String campaignId, Metric metric, Instant from, Instant to, long uniqueCustomers) {
}
