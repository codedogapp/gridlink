---
title: Home
nav_order: 1
---

# GridLink

Bind [ag-grid](https://www.ag-grid.com/)'s server-side / infinite row model straight to
[Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch) &mdash; no mapping code.

ag-grid's `sortModel` / `filterModel` JSON binds into typed Java records, and one call turns a request
into a ready-to-run `CriteriaQuery`.

Production tested and validated.

## Guide

| Page | Contents |
| --- | --- |
| [Getting started](getting-started.md) | End-to-end: model a grid, accept the request, run the query. |
| [Filtering](filtering.md) | Text and date filters, compound `AND`/`OR`, multi-column, and the query each produces. |
| [Sorting & paging](sorting-and-paging.md) | Multi-column sort and how the row window maps to a page. |
| [Elasticsearch adapter](elasticsearch-adapter.md) | The `ElasticsearchQueries` facade and its translation rules. |
| [Architecture](architecture.md) | Module layout, design decisions, and version compatibility. |

## API reference

[Full Javadoc](apidocs/index.html).

## Modules

| Module | What it is |
| --- | --- |
| `gridlink-core` | Framework-agnostic model of ag-grid's filter / sort / grid request. Zero runtime dependencies. |
| `gridlink-elasticsearch` | Adapter translating that model into Spring Data Elasticsearch `Criteria` / `Sort` / `CriteriaQuery`. |
