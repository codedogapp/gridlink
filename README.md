# GridLink

[![build](https://github.com/codedogapp/gridlink/actions/workflows/ci.yml/badge.svg)](https://github.com/codedogapp/gridlink/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00.svg?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Bind [ag-grid](https://www.ag-grid.com/)'s server-side / infinite row model straight to
[Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch) &mdash; **no mapping code**.

ag-grid's `sortModel` / `filterModel` JSON binds into typed Java records, and one call turns a request
into a ready-to-run `CriteriaQuery`:

```java
CriteriaQuery query = ElasticsearchQueries.toQuery(request);   // criteria + sort + paging
SearchHits<Product> hits = operations.search(query, Product.class);
```

```
ag-grid JSON ──bind──▶ GridRequest<F> ──ElasticsearchQueries.toQuery──▶ CriteriaQuery ──▶ OpenSearch / Elasticsearch
```

## Modules

| Module | What it is | Depends on |
| --- | --- | --- |
| [`gridlink-core`](gridlink-core) | Framework-agnostic model of ag-grid's filter / sort / grid request. Zero runtime dependencies. | &mdash; |
| [`gridlink-elasticsearch`](gridlink-elasticsearch) | Adapter translating that model into Spring Data Elasticsearch `Criteria` / `Sort` / `CriteriaQuery`. | `gridlink-core`, `spring-data-elasticsearch` (`provided`) |

## Install

Not yet on Maven Central &mdash; build and install to your local repo:

```bash
git clone https://github.com/codedogapp/gridlink.git
cd gridlink
mvn -B install -DskipTests
```

Then depend on the adapter (it pulls in `gridlink-core`; your app already provides
`spring-data-elasticsearch`):

```xml
<dependency>
    <groupId>io.github.codedogapp</groupId>
    <artifactId>gridlink-elasticsearch</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Quickstart

**1. Describe your grid's filterable columns.** One typed component per column; `filters()` maps each to
its target document field:

```java
public record ProductFilterModel(
    FieldFilter name,
    FieldFilter category,
    DateFieldFilter createdAt
) implements FilterModel {

    @Override
    public Map<String, ColumnFilter> filters() {
        var columns = new LinkedHashMap<String, ColumnFilter>();
        columns.put("name", name);
        columns.put("category", category);
        columns.put("createdAt", createdAt);
        return columns;
    }
}
```

**2. Accept the request and run the query.** ag-grid's JSON binds into `GridRequest<ProductFilterModel>`:

```java
@PostMapping("/api/products/query")
public GridResponse<Product> query(@RequestBody GridRequest<ProductFilterModel> request) {
    SearchHits<Product> hits = operations.search(ElasticsearchQueries.toQuery(request), Product.class);
    var rows = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    return new GridResponse<>(rows, hits.getTotalHits());
}
```

That is the whole integration. Filtering, compound `AND`/`OR`, date ranges, multi-column filters, sorting
and paging all fall out of the bound request.

See [**Getting started**](docs/getting-started.md) for the end-to-end walkthrough.

## Documentation

- **Guide** &mdash; [docs/](docs/index.md) &middot; hosted at <https://codedogapp.github.io/gridlink/>
  - [Getting started](docs/getting-started.md)
  - [Filtering](docs/filtering.md)
  - [Sorting & paging](docs/sorting-and-paging.md)
  - [Elasticsearch adapter](docs/elasticsearch-adapter.md)
  - [Architecture](docs/architecture.md)
- **API reference (Javadoc)** &mdash; <https://codedogapp.github.io/gridlink/apidocs/>
- **Runnable example** &mdash; [`examples/opensearch-aggrid-demo`](examples/opensearch-aggrid-demo) (ag-grid + Spring Boot 4 + OpenSearch)

## Compatibility

| | Version |
| --- | --- |
| Java (libraries) | 17+ |
| spring-data-elasticsearch | 5.x (Spring Boot 3) and 6.x (Spring Boot 4) &mdash; one artifact, [verified in CI](.github/workflows/ci.yml) |

## License

[MIT](LICENSE)
