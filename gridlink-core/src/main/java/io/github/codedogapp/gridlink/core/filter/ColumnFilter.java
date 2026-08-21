package io.github.codedogapp.gridlink.core.filter;


/**
 * Sealed supertype of the two gridlink column-filter models, {@link FieldFilter} (text) and
 * {@link DateFieldFilter} (date).
 *
 * <p>It lets a {@link FilterModel} expose its columns as a type-safe {@code Map<String, ColumnFilter>}
 * — the consumer chooses each column's target field name (the map key) explicitly, with no reflection
 * and no {@code Object} values.
 */
public sealed interface ColumnFilter permits FieldFilter, DateFieldFilter {

}
