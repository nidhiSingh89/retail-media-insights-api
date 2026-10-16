# Campaign Insights API (simple version)

A small Java 21 / Spring Boot service that answers: **how many unique customers** saw, clicked, or
clicked-then-added-to-basket for an ad campaign?

## Endpoints

All three take the same inputs:

| Endpoint | Counts unique customers who... |
|---|---|
| `GET /ad/{campaignId}/impressions` | saw an ad of the campaign |
| `GET /ad/{campaignId}/clicks` | clicked an ad of the campaign |
| `GET /ad/{campaignId}/clickToBasket` | added a promoted product to their cart after clicking |

- Header `X-Tenant-Id` (required): the retailer making the call.
- Query `from`, `to` (optional, ISO-8601 UTC). Default: the last 7 days. The longest range allowed is 90 days.

Example response:

```json
{ "campaignId": "cmp-1001", "metric": "clicks",
  "from": "2026-10-09T10:00:00Z", "to": "2026-10-16T10:00:00Z", "uniqueCustomers": 968 }
```

Errors return JSON problem details:
- **400**: missing header, bad date, or invalid range.
- **404**: the campaign is unknown *for this tenant*. Another tenant's campaign also returns 404.

## Run it

```bash
./mvnw verify            # build + run tests
./mvnw spring-boot:run   # starts on :8080 with synthetic demo data

curl -H 'X-Tenant-Id: retailer-a' localhost:8080/ad/cmp-1001/clicks
curl -H 'X-Tenant-Id: retailer-a' 'localhost:8080/ad/cmp-1001/clickToBasket?from=2026-10-10T00:00:00Z&to=2026-10-16T00:00:00Z'
curl -H 'X-Tenant-Id: retailer-b' localhost:8080/ad/cmp-1001/clicks   # 404: not your campaign
```

The demo data is random and synthetic. It contains no real customers. Set `insights.sample-data=false` to switch it off.

## Code layout

```
api/         CampaignInsightsController, response record, error handler
service/     CampaignInsightsService (validation, tenant check), Metric enum
repository/  MetricRepository (interface) + InMemoryMetricRepository, sample-data loader
```

## What production would change

- **Storage:** a Redis implementation of `MetricRepository` would use HyperLogLog keys per
  campaign/metric/hour, written by Flink. `PFCOUNT` over the hours in the range gives the unique
  count, at about 1% error and in very little memory. The in-memory version here counts exactly.
- **Tenant:** the API gateway would set the tenant from the caller's authenticated token, so the
  client could not choose it.
- **Click-to-basket:** Flink matches each click to a later add-to-cart within the attribution window.
  This API only reads the result.
