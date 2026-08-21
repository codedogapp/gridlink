# gridlink · ag-grid + Spring Boot 4 + OpenSearch demo

A light, end-to-end example that exercises [gridlink](../../) for real: an
[ag-grid](https://www.ag-grid.com/) table talks to a Spring Boot 4 backend, which turns ag-grid's
sort/filter model into a Spring Data `CriteriaQuery` via `gridlink-elasticsearch` and runs it against
OpenSearch.

It is intentionally small — one `products` index, ~32 seeded rows — but wires the full path so you can
poke filters/sorts in a browser and see the generated queries hit a real cluster.

## Stack

| Piece            | Version                                             |
| ---------------- | --------------------------------------------------- |
| Spring Boot      | 4.1.0 (`spring-boot-starter-webmvc`, Jackson 3)     |
| Data access      | `spring-data-opensearch-starter` 3.1.1 (sde 6.1.x)  |
| gridlink         | `io.github.codedogapp:gridlink-elasticsearch:0.0.1` |
| OpenSearch       | 3.x server                                          |
| ag-grid          | Community v33 (infinite row model)                  |
| JDK              | 21+                                                 |

> spring-data-opensearch reuses spring-data-elasticsearch's `Criteria`/`Sort` core and swaps only the
> transport, so gridlink's output works unchanged against OpenSearch (and avoids the ES product-check).

## Prerequisites

- JDK 21+
- A container engine: **docker** or **podman**
- gridlink installed to your local Maven repo (from the repo root):

  ```bash
  mise exec -- mvn -B install -DskipTests
  ```

## 1. Start OpenSearch

**docker compose** (or `podman compose` if a provider is configured):

```bash
docker compose up -d        # uses compose.yaml in this folder
```

**podman run** (fallback used during development — no compose provider needed):

```bash
podman run -d --name gridlink-opensearch \
  -p 9200:9200 -p 9600:9600 \
  -e discovery.type=single-node \
  -e plugins.security.disabled=true \
  -e OPENSEARCH_JAVA_OPTS='-Xms512m -Xmx512m' \
  -e OPENSEARCH_INITIAL_ADMIN_PASSWORD='Gridlink_Demo_2026!' \
  opensearchproject/opensearch:3
```

Wait for green:

```bash
curl -s http://localhost:9200/_cluster/health | jq .status   # "green"
```

## 2. Run the app

```bash
mise exec -- mvn -DskipTests clean package
mise exec -- java -jar target/opensearch-aggrid-demo-0.0.1-SNAPSHOT.jar
```

On startup `DataSeeder` recreates the `products` index (keyword fields + `lowercase_normalizer`) and
seeds ~32 rows. Then open <http://localhost:8080>.

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

## How columns are combined (important)

`ElasticsearchQueries.toCriteria(filterModel)` attaches each column's gridlink criteria to the root via
**`root.subCriteria(fieldCriteria)`**, *not* `root.and(fieldCriteria)`.

Spring Data's `Criteria` threads conditions through a shared, mutable criteria chain. A compound criteria
(e.g. an `OR` of two `contains` on the same field) carries its branches inside its own chain. Combining
such a criteria with `and(...)` only splices the criteria's final node into the parent chain and silently
drops the earlier branches — so `name contains "mac" OR contains "iphone"` collapses to just `*iphone*`.
`subCriteria(...)` nests the whole thing as a grouped `bool`, preserving every branch
(`must[ bool{ should:[*mac*, *iphone*] } ]`). This mirrors the production pattern the helpers were
extracted from.

## Binding ag-grid's model straight into gridlink types

gridlink's model records mirror ag-grid's filter/sort JSON field names, so ag-grid's payload binds
directly into them — no mapping code. `gridlink-core` ships the generic request/response plus the
`FilterModel` interface; the demo writes its own columns and maps them to field names:

```java
// gridlink-core (zero-dependency):
//   record GridRequest<F extends FilterModel>(int startRow, int endRow, List<SortModel> sortModel, F filterModel)
//   record GridResponse<T>(List<T> rows, long lastRow)
//   sealed interface ColumnFilter permits FieldFilter, DateFieldFilter {}
//   interface FilterModel { Map<String, ColumnFilter> filters(); }

// the demo supplies its filterable columns and names each column's target field:
record ProductFilterModel(FieldFilter name,           // text columns -> FieldFilter
                          FieldFilter category,
                          DateFieldFilter createdAt    // date column  -> DateFieldFilter
) implements FilterModel {
    public Map<String, ColumnFilter> filters() {
        var columns = new LinkedHashMap<String, ColumnFilter>();
        columns.put("name", name);            // map key = target field name, chosen by you
        columns.put("category", category);
        columns.put("createdAt", createdAt);
        return columns;
    }
}

// the controller signature is then fully typed and binds ag-grid JSON with zero mapping code:
GridResponse<Product> query(@RequestBody GridRequest<ProductFilterModel> request) { ... }

// and the whole request (filter + sort + paging) becomes a ready-to-run CriteriaQuery in one call:
CriteriaQuery query = ElasticsearchQueries.toQuery(request);   // no per-column, sort, or paging code
```

- `GridRequest<F extends FilterModel>` and `GridResponse<T>` live in `gridlink-core`; a consumer only
  provides a `FilterModel` (`ProductFilterModel`) and its row type (`Product`).
- `SortModel(colId, sort)` and `FieldFilter { type, filter, operator, conditions }` /
  `DateFieldFilter { type, dateFrom, dateTo, operator, conditions }` match ag-grid's keys exactly.
- `ElasticsearchQueries.toQuery(request)` is the one-call entry point: it builds the `Criteria` (below),
  applies the `sortModel`, and pages using `GridRequest`'s derived `pageNumber()` / `limit()`. Those
  accessors clamp a negative `startRow` and fall back to `GridRequest.DEFAULT_PAGE_SIZE` for an empty or
  malformed window, while the raw `startRow` / `endRow` stay untouched for round-tripping. Reach for the
  lower-level `toCriteria(...)` / `sorts(...)` only when building a different query type.
- `ElasticsearchQueries.toCriteria(FilterModel)` reads the model's `filters()` map: each entry's key is
  the target field and its value (a `ColumnFilter` — `FieldFilter` or `DateFieldFilter`) is dispatched
  to the right criteria builder. You choose the field names, so a column can filter a differently named
  field; `null` columns are skipped; the result is combined via `subCriteria` (see above). No reflection.
- ag-grid tags each filter node with an extra `filterType` (and legacy `condition1`/`condition2`) not in
  the gridlink model; Spring Boot 4 / Jackson 3 ignore unknown JSON properties by default, so no config
  is needed.

## Files

- `compose.yaml` — single-node OpenSearch (security disabled, ports 9200/9600).
- `Product.java` / `DataSeeder.java` — document mapping + seed data.
- `api/dto/ProductFilterModel.java` — the demo's only request type: its columns bind straight from ag-grid JSON into gridlink model types.
- `api/service/ProductService.java` — turns the whole request into a ready-to-run `CriteriaQuery` via `ElasticsearchQueries.toQuery(request)` (criteria + sorts + paging) and executes it.
- `api/controller/ProductController.java` — thin `@PostMapping("/query")` that delegates to the service.
- `src/main/resources/application.yml` — app config (`server.port`).
- `src/main/resources/static/{index.html,styles.css,app.js}` — the ag-grid front end (markup, styles, script).

## Teardown

```bash
docker compose down            # or: podman rm -f gridlink-opensearch
```
