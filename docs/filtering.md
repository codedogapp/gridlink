---
title: Filtering
nav_order: 3
---

# Filtering

A [`FilterModel`](apidocs/io/github/codedogapp/gridlink/core/filter/FilterModel.html) exposes its columns as a `Map<String, ColumnFilter>` &mdash; field name &rarr; filter. 
Each value is one of the two sealed [`ColumnFilter`](apidocs/io/github/codedogapp/gridlink/core/filter/ColumnFilter.html) types:

| Type | Column kind | ag-grid `filterType` |
| --- | --- | --- |
| [`FieldFilter`](apidocs/io/github/codedogapp/gridlink/core/filter/FieldFilter.html) | text | `text` |
| [`DateFieldFilter`](apidocs/io/github/codedogapp/gridlink/core/filter/DateFieldFilter.html) | date | `date` |

Every type mirrors ag-grid's filter JSON, so a request payload binds straight into it. Text values are
matched **case-insensitively** (the adapter lowercases the query term).

## Text filters

A simple text filter is a `type` + `filter` value:

```json
{
  "name": {
    "filterType": "text",
    "type": "contains",
    "filter": "mac"
  }
}
```

[`TextFilterType`](apidocs/io/github/codedogapp/gridlink/core/filter/TextFilterType.html) and the query
each produces for value `mac` on field `name`:

| `type` | Matches | Elasticsearch query |
| --- | --- | --- |
| `contains` | field contains the value | `name ~ *mac*` |
| `notContains` | field does not contain the value | `NOT name ~ *mac*` |
| `equals` | field equals the value | `name = mac` |
| `notEqual` | field differs from the value | `NOT name = mac` |
| `startsWith` | field starts with the value | `name ~ mac*` |
| `endsWith` | field ends with the value | `name ~ *mac` |
| `blank` | field is missing / empty | `name` does not exist |
| `notBlank` | field is present | `name` exists |

A blank/absent `filter` value drops the condition (it becomes match-all for that column) &mdash; except for
`blank` / `notBlank`, which need no value.

## Date filters

Dates are ISO strings (`yyyy-MM-dd`; anything after the day is ignored). `inRange` uses both bounds, every
other type uses `dateFrom`:

```json
{
  "createdAt": {
    "type": "inRange",
    "dateFrom": "2024-01-01",
    "dateTo": "2024-12-31"
  }
}
```

[`DateFilterType`](apidocs/io/github/codedogapp/gridlink/core/filter/DateFilterType.html) for field
`createdAt`:

| `type` | Matches | Elasticsearch query |
| --- | --- | --- |
| `equals` | on that day | `createdAt` in `[day 00:00:00 … 23:59:59]` |
| `notEqual` | not on that day | NOT in `[day 00:00:00 … 23:59:59]` |
| `greaterThan` | after that day starts | `createdAt > day 00:00:00` |
| `lessThan` | before that day starts | `createdAt < day 00:00:00` |
| `inRange` | within `dateFrom`…`dateTo` | `createdAt` in `[from 00:00:00 … to 23:59:59]` |

`equals` / `inRange` expand to a full-day (or full-range) window, so they match regardless of the stored
time-of-day. An unparseable or missing date drops the condition.

## Compound filters (AND / OR)

ag-grid's "two conditions" mode sends an `operator` plus a `conditions` array. It works identically for
text and dates:

```json
{
  "name": {
    "filterType": "text",
    "operator": "OR",
    "conditions": [
      {
        "type": "contains",
        "filter": "mac"
      },
      {
        "type": "contains",
        "filter": "iphone"
      }
    ]
  }
}
```

The conditions are combined with [`FilterOperator`](apidocs/io/github/codedogapp/gridlink/core/filter/FilterOperator.html)
`AND` or `OR` &rarr; `name ~ *mac*  OR  name ~ *iphone*`.

## Multiple columns

Every active column in the map is combined into the final query. Columns are joined as **grouped**
sub-queries, so a compound `OR`/`AND` on one column keeps its own branches instead of collapsing into a
neighbouring column's clause:

```json
{
  "filterModel": {
    "category": {
      "filterType": "text",
      "type": "equals",
      "filter": "electronics"
    },
    "name": {
      "filterType": "text",
      "operator": "OR",
      "conditions": [
        {
          "type": "contains",
          "filter": "mac"
        },
        {
          "type": "contains",
          "filter": "iphone"
        }
      ]
    }
  }
}
```

&rarr; `category = electronics  AND  (name ~ *mac*  OR  name ~ *iphone*)`. 

See[Elasticsearch adapter](elasticsearch-adapter.md#grouping-preserves-branches) for why this uses `subCriteria` rather than `and`.

## Building filters in Java

For tests or server-side filters, use the fluent builders instead of JSON. Every property is optional and
defaults to `null`:

```java
// contains "mac"
FieldFilter.builder()
    .type(TextFilterType.contains)
    .filter("mac")
    .build();

// name contains "mac" OR "iphone"
FieldFilter.builder()
    .operator(FilterOperator.OR)
    .conditions(List.of(
        FieldFilter.builder().type(TextFilterType.contains).filter("mac").build(),
        FieldFilter.builder().type(TextFilterType.contains).filter("iphone").build()))
    .build();

// createdAt within 2024
DateFieldFilter.builder()
    .type(DateFilterType.inRange)
    .dateFrom("2024-01-01")
    .dateTo("2024-12-31")
    .build();
```
