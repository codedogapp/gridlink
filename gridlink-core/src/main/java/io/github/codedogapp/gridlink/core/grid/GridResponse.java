package io.github.codedogapp.gridlink.core.grid;

import java.util.List;


/**
 * Response for ag-grid's infinite / server-side row model: a page of {@code rows} plus the total
 * match count. Generic over the row type, so consumers only supply their own domain type.
 *
 * @param rows    the page of matching rows
 * @param lastRow total number of matching rows, so the grid knows where the dataset ends
 * @param <T>     the row type
 */
public record GridResponse<T>(
    List<T> rows,
    long lastRow
) {

}
