---
title: Sorting & paging
nav_order: 4
---

# Sorting & paging

Both are carried by [`GridRequest`](apidocs/io/github/codedogapp/gridlink/core/grid/GridRequest.html) and
applied for you by [`ElasticsearchQueries.toQuery`](apidocs/io/github/codedogapp/gridlink/elasticsearch/ElasticsearchQueries.html).
The raw ag-grid fields stay untouched; sort and page are *derived* from them.

## Sorting

ag-grid's `sortModel` is a list of [`SortModel`](apidocs/io/github/codedogapp/gridlink/core/sort/SortModel.html)
entries &mdash; `colId` + [`SortDirection`](apidocs/io/github/codedogapp/gridlink/core/sort/SortDirection.html)
(`asc` / `desc`) &mdash; in **priority order**:

```json
"sortModel": [
  {
    "colId": "category",
    "sort": "asc"
  },
  {
    "colId": "price",
    "sort": "desc"
  }
]
```

&rarr; sort by `category` ascending, then `price` descending.

`toQuery` attaches these to the `CriteriaQuery` in order. If you build a different query type yourself, map
them with [`sorts(List<SortModel>)`](apidocs/io/github/codedogapp/gridlink/elasticsearch/ElasticsearchQueries.html),
which returns one Spring Data `Sort` per entry:

```java
List<Sort> sorts = ElasticsearchQueries.sorts(request.sortModel());
sorts.forEach(nativeQueryBuilder::withSort);
```

`Sort` is used rather than an Elasticsearch query-builder type because it is a stable
`spring-data-commons` type, identical across the 5.x and 6.x lines.

## Paging

ag-grid requests a half-open row window `[startRow, endRow)`. `GridRequest` derives a page from it:

| Method | Value | Notes |
| --- | --- | --- |
| `offset()` | `max(startRow, 0)` | negative `startRow` clamped to `0` |
| `limit()` | `endRow - offset()`, else `DEFAULT_PAGE_SIZE` | falls back to `100` when the window is empty or malformed |
| `pageNumber()` | `offset() / limit()` | zero-based page index |

`toQuery` turns that into `PageRequest.of(pageNumber(), limit())`. Because ag-grid always requests blocks
whose `startRow` is a multiple of its cache block size, `offset()` is a whole multiple of `limit()` and the
page index addresses the requested window exactly.

Examples:

| `startRow` | `endRow` | `offset()` | `limit()` | `pageNumber()` |
| ---: | ---: | ---: | ---: | ---: |
| 0 | 100 | 0 | 100 | 0 |
| 100 | 200 | 100 | 100 | 1 |
| 0 | 50 | 0 | 50 | 0 |
| 50 | 100 | 50 | 50 | 1 |
| -5 | 0 | 0 | 100 | 0 |

## Reporting the total

Pair the page with the total match count in
[`GridResponse`](apidocs/io/github/codedogapp/gridlink/core/grid/GridResponse.html) so ag-grid's infinite
model knows where the dataset ends:

```java
return new GridResponse<>(rows, hits.getTotalHits());
```
