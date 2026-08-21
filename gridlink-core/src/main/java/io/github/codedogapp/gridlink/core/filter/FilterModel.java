package io.github.codedogapp.gridlink.core.filter;

import java.util.Map;


/**
 * A consumer-defined filter model that exposes its filterable columns as a map of
 * <em>field name → {@link ColumnFilter}</em>.
 *
 * <p>The map keys are the target document field names, chosen by the consumer and independent of any
 * field or record-component name — so a column can filter a differently named field. An adapter such
 * as {@code ElasticsearchQueries.toCriteria(FilterModel)} turns each entry into a query; entries with
 * a {@code null} value (e.g. a column with no active filter) are ignored. Return an order-preserving
 * map (e.g. {@link java.util.LinkedHashMap}) if a stable column order matters.
 *
 * <p>Typically implemented by a record whose components ag-grid's {@code filterModel} JSON binds into,
 * with {@link #filters()} mapping those components to field names:
 * <pre>{@code
 * record ProductFilterModel(FieldFilter name, FieldFilter category, DateFieldFilter createdAt)
 *         implements FilterModel {
 *     public Map<String, ColumnFilter> filters() {
 *         var columns = new LinkedHashMap<String, ColumnFilter>();
 *         columns.put("name", name);
 *         columns.put("category", category);
 *         columns.put("createdAt", createdAt);
 *         return columns;
 *     }
 * }
 * }</pre>
 */
public interface FilterModel {

    /**
     * @return this model's columns as a map of field name to its {@link ColumnFilter}; values may be
     * {@code null} for columns with no active filter.
     */
    Map<String, ColumnFilter> filters();

}
