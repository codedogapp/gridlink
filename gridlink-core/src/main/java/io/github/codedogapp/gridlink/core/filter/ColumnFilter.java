package io.github.codedogapp.gridlink.core.filter;


/**
 * Sealed supertype of the gridlink column-filter models: {@link FieldFilter} (text),
 * {@link NumberFieldFilter} (number) and {@link DateFieldFilter} (date).
 *
 * <p>It lets a {@link FilterModel} expose its columns as a type-safe {@code Map<String, ColumnFilter>}
 * — the consumer chooses each column's target field name (the map key) explicitly, with no reflection
 * and no {@code Object} values.
 *
 * <p>ag-grid's set and multi-column filters are not modelled yet (coming soon).
 */
public sealed interface ColumnFilter permits FieldFilter, NumberFieldFilter, DateFieldFilter {

}
