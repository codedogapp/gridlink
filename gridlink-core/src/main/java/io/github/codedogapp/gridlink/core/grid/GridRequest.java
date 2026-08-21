package io.github.codedogapp.gridlink.core.grid;

import io.github.codedogapp.gridlink.core.filter.FilterModel;
import io.github.codedogapp.gridlink.core.sort.SortModel;

import java.util.List;


/**
 * Request for ag-grid's infinite / server-side row model. Generic over the consumer's
 * {@link FilterModel} implementation, so ag-grid's {@code sortModel} / {@code filterModel} JSON binds
 * straight into typed gridlink model types with no mapping code.
 *
 * @param startRow    first row index requested (inclusive)
 * @param endRow      last row index requested (exclusive)
 * @param sortModel   ag-grid sort model, in priority order
 * @param filterModel ag-grid filter model, one typed entry per filterable column
 * @param <F>         the consumer's {@link FilterModel} type
 */
public record GridRequest<F extends FilterModel>(
    int startRow,
    int endRow,
    List<SortModel> sortModel,
    F filterModel
) {

    /**
     * Page size used when {@link #endRow()} does not exceed the clamped {@link #offset()}.
     */
    public static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * Zero-based, non-negative row offset. A negative {@link #startRow()} is clamped to {@code 0}.
     *
     * @return {@code max(startRow, 0)}
     */
    public int offset() {
        return Math.max(startRow, 0);
    }

    /**
     * Number of rows requested. Derived from the ag-grid {@code startRow} / {@code endRow} window,
     * falling back to {@link #DEFAULT_PAGE_SIZE} when the window is empty or malformed.
     *
     * @return {@code endRow - offset()} when positive, otherwise {@link #DEFAULT_PAGE_SIZE}
     */
    public int limit() {
        final int offset = offset();
        return endRow > offset
            ? endRow - offset
            : DEFAULT_PAGE_SIZE;
    }

    /**
     * Zero-based page index for a fixed-size pager (e.g. Spring Data {@code PageRequest}).
     * <p>
     * ag-grid always requests blocks whose {@code startRow} is a multiple of its cache block size, so
     * {@link #offset()} is a whole multiple of {@link #limit()} and this index addresses the window
     * exactly. For an arbitrary (non-block-aligned) offset it is the integer-division page it falls in.
     *
     * @return {@code offset() / limit()}
     */
    public int pageNumber() {
        return offset() / limit();
    }

}
