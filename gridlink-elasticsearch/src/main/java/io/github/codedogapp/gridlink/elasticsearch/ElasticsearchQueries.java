package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.ColumnFilter;
import io.github.codedogapp.gridlink.core.filter.DateFieldFilter;
import io.github.codedogapp.gridlink.core.filter.FieldFilter;
import io.github.codedogapp.gridlink.core.filter.FilterModel;
import io.github.codedogapp.gridlink.core.grid.GridRequest;
import io.github.codedogapp.gridlink.core.sort.SortModel;

import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * High-level facade turning GridLink filter/sort models into Spring Data Elasticsearch
 * {@link Criteria}, {@link Sort} and a ready-to-run {@link CriteriaQuery}.
 * <p>
 * {@link #toQuery(GridRequest)} is the one-call entry point (criteria + sort + paging); the lower-level
 * {@link #toCriteria(FilterModel)} and {@link #sorts(List)} remain exposed for callers that build a
 * different query type. Sorting is offered as {@code List<Sort>} because {@code Sort} is a rock-stable
 * spring-data-commons type, whereas the Elasticsearch query-builder types have shifted between
 * versions; attach it yourself with e.g.
 * {@code ElasticsearchQueries.sorts(list).forEach(builder::withSort)}.
 */
public final class ElasticsearchQueries {

    private ElasticsearchQueries() {
    }

    /**
     * Translates a text {@link FieldFilter} (simple or compound) into a {@link Criteria}.
     */
    public static @Nullable Criteria toCriteria(final String field, final @Nullable FieldFilter filter) {
        if (filter == null) {
            return null;
        }

        if (filter.conditions() != null && !filter.conditions().isEmpty()) {
            final var criteria = filter.conditions().stream()
                .map(c -> ElasticsearchCriteria.text(c.type(), field, c.filter()))
                .filter(Objects::nonNull)
                .toList();
            return ElasticsearchCriteria.chain(filter.operator(), criteria);
        } else if (filter.type() != null) {
            return ElasticsearchCriteria.text(filter.type(), field, filter.filter());
        }

        return null;
    }

    /**
     * Translates a {@link DateFieldFilter} (simple, {@code inRange}, or compound) into a {@link Criteria}.
     */
    public static @Nullable Criteria toCriteria(final String field, final @Nullable DateFieldFilter filter) {
        if (filter == null) {
            return null;
        }

        if (filter.conditions() != null && !filter.conditions().isEmpty()) {
            final var criteria = filter.conditions().stream()
                .map(c -> ElasticsearchCriteria.date(c.type(), field, c.dateFrom(), c.dateTo()))
                .filter(Objects::nonNull)
                .toList();
            return ElasticsearchCriteria.chain(filter.operator(), criteria);
        } else if (filter.type() != null) {
            return ElasticsearchCriteria.date(filter.type(), field, filter.dateFrom(), filter.dateTo());
        }

        return null;
    }

    /**
     * Translates a whole {@link FilterModel} into a single root {@link Criteria} from the model's
     * {@link FilterModel#filters() filters()} map. Equivalent to {@code toCriteria(model.filters())};
     * a {@code null} model yields an empty (match-all) root.
     *
     * @param filterModel the consumer's filter model, or {@code null} for no filtering
     * @return a root {@link Criteria}; empty (match-all) when there are no active columns
     */
    public static Criteria toCriteria(final @Nullable FilterModel filterModel) {
        return filterModel == null ? new Criteria() : toCriteria(filterModel.filters());
    }

    /**
     * Translates a map of <em>field name → {@link ColumnFilter}</em> into a single root
     * {@link Criteria}. Each entry's key is the target field and its value is dispatched to the
     * matching per-column translator ({@link FieldFilter} → text, {@link DateFieldFilter} → date);
     * {@code null} values (columns with no active filter) are skipped.
     *
     * <p>Every column's criteria is attached with {@link Criteria#subCriteria(Criteria)} rather than
     * {@link Criteria#and(Criteria)}. {@code and} splices only the other criteria's final node into
     * the shared chain and drops earlier branches, so a compound OR/AND on one field would collapse
     * to its last condition; {@code subCriteria} nests the whole thing as a grouped {@code bool},
     * preserving every branch.
     *
     * @param filters the field-to-filter map, or {@code null}
     * @return a root {@link Criteria}; empty (match-all) when {@code filters} is {@code null} or has no
     *     active columns
     */
    public static Criteria toCriteria(final @Nullable Map<String, ? extends ColumnFilter> filters) {
        final Criteria root = new Criteria();
        if (filters == null) {
            return root;
        }

        for (final Map.Entry<String, ? extends ColumnFilter> column : filters.entrySet()) {
            final Criteria columnCriteria = columnCriteria(column.getKey(), column.getValue());
            if (columnCriteria != null) {
                root.subCriteria(columnCriteria);
            }
        }
        return root;
    }

    private static @Nullable Criteria columnCriteria(final String field, final @Nullable ColumnFilter filter) {
        if (filter instanceof FieldFilter text) {
            return toCriteria(field, text);
        }
        if (filter instanceof DateFieldFilter date) {
            return toCriteria(field, date);
        }
        return null;
    }

    /**
     * Assembles a ready-to-run {@link CriteriaQuery} from a whole {@link GridRequest}: filter criteria
     * (via {@link #toCriteria(FilterModel)}), sorting (via {@link #sorts(List)}) and a page derived from
     * the request's {@link GridRequest#pageNumber() pageNumber()} / {@link GridRequest#limit() limit()}.
     * The caller only runs it, e.g. {@code operations.search(ElasticsearchQueries.toQuery(request), Row.class)}.
     *
     * @param request the ag-grid request; its {@code filterModel} may be {@code null} (match-all)
     * @return a {@link CriteriaQuery} with criteria, sort and pageable applied
     */
    public static CriteriaQuery toQuery(final GridRequest<?> request) {
        final CriteriaQuery query = new CriteriaQuery(toCriteria(request.filterModel()));
        sorts(request.sortModel()).forEach(query::addSort);
        query.setPageable(PageRequest.of(request.pageNumber(), request.limit()));
        return query;
    }

    /**
     * Maps ag-grid {@link SortModel} entries to Spring Data {@link Sort} instances, in order.
     *
     * @return one {@link Sort} per entry; empty when the input is {@code null} or empty.
     */
    public static List<Sort> sorts(final @Nullable List<SortModel> sortModel) {
        if (sortModel == null || sortModel.isEmpty()) {
            return List.of();
        }

        return sortModel.stream()
            .map(s -> Sort.by(ElasticsearchCriteria.direction(s.sort()), s.colId()))
            .toList();
    }

}
