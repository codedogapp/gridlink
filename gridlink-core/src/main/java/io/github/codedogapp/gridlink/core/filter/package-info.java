/**
 * ag-grid filter model. {@link io.github.codedogapp.gridlink.core.filter.FilterModel} exposes a
 * <em>field&nbsp;name&nbsp;&rarr;&nbsp;column filter</em> map whose values are the sealed
 * {@link io.github.codedogapp.gridlink.core.filter.ColumnFilter} type
 * ({@link io.github.codedogapp.gridlink.core.filter.FieldFilter} for text,
 * {@link io.github.codedogapp.gridlink.core.filter.NumberFieldFilter} for numbers,
 * {@link io.github.codedogapp.gridlink.core.filter.DateFieldFilter} for dates), together with the
 * ag-grid enums {@link io.github.codedogapp.gridlink.core.filter.TextFilterType},
 * {@link io.github.codedogapp.gridlink.core.filter.NumberFilterType},
 * {@link io.github.codedogapp.gridlink.core.filter.DateFilterType} and
 * {@link io.github.codedogapp.gridlink.core.filter.FilterOperator}. Every type mirrors ag-grid's
 * filter JSON so a request payload binds straight into it with no mapping code.
 *
 * <p>ag-grid's set and multi-column filters are not modelled yet (coming soon).
 */
package io.github.codedogapp.gridlink.core.filter;
