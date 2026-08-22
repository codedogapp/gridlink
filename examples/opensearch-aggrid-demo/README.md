# gridlink · ag-grid + Spring Boot 4 + OpenSearch demo

A light, end-to-end example that exercises [gridlink](../../) for real: an
[ag-grid](https://www.ag-grid.com/) table talks to a Spring Boot 4 backend, which turns ag-grid's
sort/filter model into a Spring Data `CriteriaQuery` via `gridlink-elasticsearch` and runs it against
OpenSearch.

It is intentionally small &mdash; one `products` catalogue &mdash; but wires the full path so you can
poke filters/sorts in a browser and see the generated queries hit a real cluster.

**Two stores, mirrored.** As in a real deployment, OpenSearch is *not* the source of truth: a local
**SQLite** database is. On first run the app builds SQLite from a checked-in SQL script (~1,200 rows,
with ids and `createdAt` baked in) and then rebuilds the OpenSearch `products` index from it. SQLite
persists across restarts; the search index is fully derived and disposable &mdash; the grid only ever queries
the index. The binary `products.db` is git-ignored, so only the plain-text `.sql` is committed.

## Prerequisites

- JDK 21+
- docker
- gridlink installed to your local Maven repo (from the repo root):

  ```bash
  mvn -B install -DskipTests
  ```

## 1. Start OpenSearch

**docker compose**:

```bash
docker compose up -d        # uses compose.yaml in this folder
```

Wait for green:

```bash
curl -s http://localhost:9200/_cluster/health | jq .status   # "green"
```

## 2. Run the app

```bash
mvn -DskipTests clean package
java -jar target/opensearch-aggrid-demo-0.0.1-SNAPSHOT.jar
```

## 3. Try it

The grid posts ag-grid's sort/filter model to `POST /api/products/query`; the backend replies with a page
of rows plus `lastRow` for the infinite model. Example:

```bash
curl -s -X POST localhost:8080/api/products/query \
  -H 'Content-Type: application/json' \
  -d '{"startRow":0,"endRow":50,
       "filterModel":{"name":{"filterType":"text","operator":"OR",
         "conditions":[{"type":"contains","filter":"mac"},
                       {"type":"contains","filter":"iphone"}]}}}'
```

Case-insensitive matching (via `lowercase_normalizer`), `contains/equals/startsWith/…`, date `inRange`,
compound `AND`/`OR` on one field, and multi-column filters all work.

To see the raw OpenSearch DSL, start the jar with `-Dlogging.level.tracer=TRACE` and grep the log for
`products/_search`.

## Teardown

```bash
docker compose down         
```
