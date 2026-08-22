package io.github.codedogapp.gridlink.core.sort;


/**
 * ag-grid compatible sort direction, matching ag-grid's sort model format.
 *
 * <p>Constants intentionally use ag-grid's exact wire tokens (lower case) so consumers can bind
 * incoming JSON to this enum by name (e.g. via Jackson). Renaming them would break that mapping.
 */
@SuppressWarnings("java:S115") // constant names mirror ag-grid's exact wire tokens; must not be renamed
public enum SortDirection {

    asc,
    desc;

}
