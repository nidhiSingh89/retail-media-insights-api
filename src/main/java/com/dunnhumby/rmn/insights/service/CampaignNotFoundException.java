package com.dunnhumby.rmn.insights.service;

/**
 * The campaign does not exist for this tenant (HTTP 404). Campaigns of other tenants also return
 * 404, so a tenant cannot discover which campaign ids exist elsewhere.
 */
public class CampaignNotFoundException extends RuntimeException {

    public CampaignNotFoundException(String campaignId) {
        super("Campaign '" + campaignId + "' not found");
    }
}
