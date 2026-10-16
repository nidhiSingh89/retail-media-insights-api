package com.dunnhumby.rmn.insights.api;

import com.dunnhumby.rmn.insights.service.UniqueCustomerResult;
import java.time.Instant;

/** JSON returned by the API. */
public record UniqueCustomerResponse(String campaignId, String metric, Instant from, Instant to, long uniqueCustomers) {

    static UniqueCustomerResponse from(UniqueCustomerResult r) {
        return new UniqueCustomerResponse(r.campaignId(), r.metric().apiName(), r.from(), r.to(), r.uniqueCustomers());
    }
}
