package io.github.codedogapp.gridlink.demo.product.api.dto;

import io.github.codedogapp.gridlink.core.filter.ColumnFilter;
import io.github.codedogapp.gridlink.core.filter.DateFieldFilter;
import io.github.codedogapp.gridlink.core.filter.FieldFilter;
import io.github.codedogapp.gridlink.core.filter.FilterModel;
import io.github.codedogapp.gridlink.core.filter.NumberFieldFilter;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * ag-grid's {@code filterModel} for the products grid, one typed component per filterable column.
 * Only columns with an active filter are present; the rest arrive as {@code null}. {@link #filters()}
 * maps each column to its target OpenSearch field name, so the grid needs no mapping code.
 *
 * @param name      text filter on the product name
 * @param category  text filter on the category
 * @param price     number filter on the price
 * @param createdAt date filter on the creation timestamp
 */
public record ProductFilterModel(
    FieldFilter name,
    FieldFilter category,
    NumberFieldFilter price,
    DateFieldFilter createdAt
) implements FilterModel {

    @Override
    public Map<String, ColumnFilter> filters() {
        final Map<String, ColumnFilter> columns = new LinkedHashMap<>();
        columns.put("name", name);
        columns.put("category", category);
        columns.put("price", price);
        columns.put("createdAt", createdAt);
        return columns;
    }

}
