---
title: Elasticsearch adapter
nav_order: 5
---

# Elasticsearch adapter

[`ElasticsearchQueries`](apidocs/io/github/codedogapp/gridlink/elasticsearch/ElasticsearchQueries.html) is
the single facade over `gridlink-core`. It is the only place that touches Spring types, and every method
is static.

## Entry points

| Method | Returns | Use |
| --- | --- | --- |
| `toQuery(GridRequest<?>)` | `CriteriaQuery` | One call: criteria + sort + paging. The usual entry point. |
| `toCriteria(FilterModel)` | `Criteria` | Just the filter criteria for a whole model. |
| `toCriteria(Map<String, ? extends ColumnFilter>)` | `Criteria` | Same, from a raw field &rarr; filter map. |
| `toCriteria(String, FieldFilter)` | `Criteria` (nullable) | One text column. |
| `toCriteria(String, DateFieldFilter)` | `Criteria` (nullable) | One date column. |
| `sorts(List<SortModel>)` | `List<Sort>` | Map ag-grid sort entries to Spring Data `Sort`. |

```java
// Everything, in one call:
CriteriaQuery query = ElasticsearchQueries.toQuery(request);

// Or compose it yourself:
Criteria criteria = ElasticsearchQueries.toCriteria(request.filterModel());
CriteriaQuery custom = new CriteriaQuery(criteria);
ElasticsearchQueries.sorts(request.sortModel()).forEach(custom::addSort);
```

## Nullability

- The whole-model `toCriteria(FilterModel)` / `toCriteria(Map)` always return a **non-null** `Criteria`.
  With no active columns it is an empty (match-all) root.
- The per-column `toCriteria(field, filter)` overloads return `null` when the filter contributes nothing
  (e.g. a blank text value). `toQuery` and the map-level methods skip such columns.

## Grouping preserves branches

When combining columns, each column's criteria is attached with `Criteria.subCriteria(...)`, **not**
`Criteria.and(...)`.

`and` splices only the *other* criteria's final node into the shared chain and drops earlier branches &mdash; so
a compound `OR`/`AND` on one column would collapse to its last condition. `subCriteria` nests the whole
column as a grouped `bool`, keeping every branch:

```
category = electronics  AND  (name ~ *mac*  OR  name ~ *iphone*)
                             └──────────── one subCriteria group ───────────┘
```

Without grouping, the `OR` branch would be lost and the query would silently return the wrong rows.

## One artifact, two Spring lines

The adapter calls only the long-stable subset of the `Criteria` API
(`where` / `is` / `not` / `between` / `expression` / `contains` / `exists` / `greaterThan` / `lessThan`),
which is identical across `spring-data-elasticsearch` **5.x** and **6.x**. The same artifact therefore serves
both Spring Boot 3 and 4; CI [verifies every supported line](https://github.com/codedogapp/gridlink/blob/main/.github/workflows/ci.yml).

`spring-data-elasticsearch` is a `provided` dependency &mdash; your application brings its own version. 
