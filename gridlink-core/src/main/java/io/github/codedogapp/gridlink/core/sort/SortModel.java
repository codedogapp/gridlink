package io.github.codedogapp.gridlink.core.sort;


/**
 * One ag-grid sort entry, mirroring ag-grid's sort model JSON exactly so it binds straight from the
 * wire with no mapping code:
 * <pre>{@code
 * { "colId": "price", "sort": "desc" }
 * }</pre>
 *
 * @param colId the column id to sort on
 * @param sort  the sort direction ({@code asc} / {@code desc})
 */
public record SortModel(
    String colId,
    SortDirection sort
) {

}
