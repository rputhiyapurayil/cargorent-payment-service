# cargorent-payment-service

Test-mode payments (card, UPI, net banking) and receipts. No real money moves.

Part of **CarGoRent** – see [cargorent-infra](../cargorent-infra/README.md) for the architecture, how to run
the whole system, demo accounts and the Postman collection.

| | |
|---|---|
| Port | 8084 |
| Schema | `cargorent_payments` (table `payments`) |
| Swagger | http://localhost:8084/swagger-ui.html |
| Calls | booking-service (load booking, mark CONFIRMED) |

## Endpoints

| Method | Path | Who |
|---|---|---|
| POST | `/payments` | customer (own booking, status PENDING_PAYMENT) |
| GET | `/payments/my`, `/payments/{id}`, `/payments/{id}/receipt`, `/payments/booking/{bookingId}` | owner or admin |
| GET | `/payments` | admin |

**Test data:** card `4111 1111 1111 1111` succeeds, `4000 0000 0000 0002` is declined; UPI `failure@upi` is declined.
Only the last 4 card digits are stored, never the CVV. If the booking cannot be confirmed after a successful charge
(e.g. it expired), the payment is marked REFUNDED.

## Run

```bash
# needs MySQL: cd ../cargorent-infra && docker compose -f docker-compose.mysql.yml up -d
mvn spring-boot:run
```

## Package layout

`com.cargorent.<service>` → `controller` · `service` · `repository` · `entity` · `dto` · `client` (calls to other services) · `common` (errors, caller identity) · `config`
