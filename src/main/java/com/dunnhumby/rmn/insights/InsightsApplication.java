package com.dunnhumby.rmn.insights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Campaign Insights API: unique customers per campaign for clicks, impressions and click-to-basket. */
@SpringBootApplication
public class InsightsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightsApplication.class, args);
    }
}
