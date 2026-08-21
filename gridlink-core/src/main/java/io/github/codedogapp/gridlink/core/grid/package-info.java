/**
 * ag-grid server-side / infinite row-model envelope: the generic
 * {@link io.github.codedogapp.gridlink.core.grid.GridRequest} (row window plus the sort and filter
 * models) and {@link io.github.codedogapp.gridlink.core.grid.GridResponse} (page of rows + total row
 * count). Paging is derived on the request via {@code offset()}, {@code limit()} and
 * {@code pageNumber()}, leaving the raw {@code startRow} / {@code endRow} untouched.
 */
package io.github.codedogapp.gridlink.core.grid;
