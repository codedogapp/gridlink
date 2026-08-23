---
title: Getting started
nav_order: 2
---

# Getting started

This walks through the full path: model a grid's columns, accept ag-grid's request, and run it against
Elasticsearch. It mirrors the runnable [`examples/opensearch-aggrid-demo`](https://github.com/codedogapp/gridlink/tree/main/examples/opensearch-aggrid-demo).

## 1. Install

Available on [Maven Central](https://central.sonatype.com/artifact/io.github.codedogapp/gridlink-elasticsearch).
Add the adapter &mdash; it pulls in `gridlink-core`:

**Maven**

```xml
<dependency>
    <groupId>io.github.codedogapp</groupId>
    <artifactId>gridlink-elasticsearch</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle**

```kotlin
implementation("io.github.codedogapp:gridlink-elasticsearch:0.1.0")
```

`gridlink-elasticsearch` declares `spring-data-elasticsearch` as `provided` &mdash; your application supplies it.

To determine the version compatible with your Spring Boot app, check [Spring's compatibility matrix](https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/versions.html)

## 2. Model the grid's columns

Implement [`FilterModel`](https://javadoc.io/doc/io.github.codedogapp/gridlink-core/latest/io/github/codedogapp/gridlink/core/filter/FilterModel.html) with one typed component per filterable column. 

`filters()` maps each component to the **document field** it targets &mdash; the key is a field name, 
not a Java name, so a column may filter a differently named field.

```java
public record ProductFilterModel(
    FieldFilter name,
    FieldFilter category,
    DateFieldFilter createdAt
) implements FilterModel {

    @Override
    public Map<String, ColumnFilter> filters() {
        final var columns = new LinkedHashMap<String, ColumnFilter>();
        columns.put("name", name);          // ag-grid column "name"      -> field "name"
        columns.put("category", category);  // ag-grid column "category"  -> field "category"
        columns.put("createdAt", createdAt);
        return columns;
    }
}
```

- Use [`FieldFilter`](https://javadoc.io/doc/io.github.codedogapp/gridlink-core/latest/io/github/codedogapp/gridlink/core/filter/FieldFilter.html) for text columns,
  [`DateFieldFilter`](https://javadoc.io/doc/io.github.codedogapp/gridlink-core/latest/io/github/codedogapp/gridlink/core/filter/DateFieldFilter.html) for date columns.
- Columns with no active filter arrive as `null` and are ignored.
- Return a `LinkedHashMap` if a stable column order matters.

## 3. Accept the request and run the query

[`GridRequest<F>`](https://javadoc.io/doc/io.github.codedogapp/gridlink-core/latest/io/github/codedogapp/gridlink/core/grid/GridRequest.html) is generic over your
`FilterModel`, so ag-grid's JSON binds straight in. One call &mdash;
[`ElasticsearchQueries.toQuery`](https://javadoc.io/doc/io.github.codedogapp/gridlink-elasticsearch/latest/io/github/codedogapp/gridlink/elasticsearch/ElasticsearchQueries.html) &mdash;
assembles criteria, sort and paging into a `CriteriaQuery`:

```java
@RestController
@RequiredArgsConstructor
class ProductController {

    private final ElasticsearchOperations operations;

    @PostMapping("/api/products/query")
    GridResponse<Product> query(@RequestBody GridRequest<ProductFilterModel> request) {
        SearchHits<Product> hits = operations.search(ElasticsearchQueries.toQuery(request), Product.class);
        var rows = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        return new GridResponse<>(rows, hits.getTotalHits());
    }
}
```

[`GridResponse<T>`](https://javadoc.io/doc/io.github.codedogapp/gridlink-core/latest/io/github/codedogapp/gridlink/core/grid/GridResponse.html) carries the page of
`rows` plus `lastRow` (the total match count) that ag-grid's infinite model needs to find the end of the
dataset.

## 4. The wire contract

**Request** &mdash; ag-grid's row window, sort model and filter model:

```json
{
  "startRow": 0,
  "endRow": 50,
  "sortModel": [
    {
      "colId": "price",
      "sort": "desc"
    }
  ],
  "filterModel": {
    "name": {
      "filterType": "text",
      "type": "contains",
      "filter": "mac"
    }
  }
}
```

**Response**:

```json
{
  "rows": [
    /* ... up to 50 Product rows ... */
  ],
  "lastRow": 122
}
```

## Next

- [Filtering](filtering.md) &mdash; every text, number and date filter and the query it produces.
- [Sorting & paging](sorting-and-paging.md) &mdash; multi-column sort and the row-window &rarr; page mapping.
- [Elasticsearch adapter](elasticsearch-adapter.md) &mdash; the lower-level `toCriteria` / `sorts` entry points.
