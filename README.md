# Docker Ladder

Two Spring Boot microservices (`catalog-service`, `orders-service`) backed by
PostgreSQL, orchestrated with Docker Compose.

- `catalog-service` owns products (name, price, stock) in a `catalog` database.
- `orders-service` owns orders in an `orders` database. When an order is
  created it calls `catalog-service` over the container network to look up
  the product and check stock before confirming the order.

## Build and run

```bash
docker compose up -d --build
```

This starts three containers on a shared bridge network (`ladder-net`):
`postgres`, `catalog-service` (port 8081), and `orders-service` (port 8082).
`orders-service` and `catalog-service` both wait on Postgres's healthcheck,
and `orders-service` also waits on `catalog-service` being healthy, before
starting.

Stop everything (and drop the data volume) with:

```bash
docker compose down -v
```

## Verify the endpoints

Create a product:

```bash
curl -X POST http://localhost:8081/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Widget","price":9.99,"stock":100}'
```

List products:

```bash
curl http://localhost:8081/products
```

Place an order (validated against the catalog):

```bash
curl -X POST http://localhost:8082/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":1,"quantity":3}'
```

Ordering an unknown product returns `422`; ordering more than the available
stock returns `409`.

List orders:

```bash
curl http://localhost:8082/orders
```

## Network and volume setup

- Both services and Postgres sit on a single user-defined bridge network
  (`ladder-net`), so `orders-service` reaches `catalog-service` at
  `http://catalog-service:8081` by container name instead of a hardcoded IP.
- Postgres data is persisted in a named volume (`pgdata`) so it survives
  container restarts and rebuilds; only `docker compose down -v` removes it.
- A single Postgres instance hosts two databases (`catalog`, `orders`);
  the second database is created by `db/init.sql`, mounted into
  `/docker-entrypoint-initdb.d/` on first boot.
- Connection details (host, port, database, credentials) are all passed to
  each service as environment variables rather than being hardcoded in the
  images.

## Dockerfiles

Each service uses a multi-stage build: a `maven` stage compiles and packages
the jar, and only the resulting jar is copied into a slim
`eclipse-temurin` JRE runtime stage. Both containers run as a dedicated
non-root `spring` user.
