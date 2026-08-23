package io.github.codedogapp.gridlink.core.filter;


/**
 * ag-grid compatible number filter types, matching ag-grid's number filter model format.
 *
 * <p>Constants intentionally use ag-grid's exact wire tokens (lower camel case) so consumers can bind
 * incoming JSON to this enum by name (e.g. via Jackson). Renaming them would break that mapping.
 */
@SuppressWarnings("java:S115") // constant names mirror ag-grid's exact wire tokens; must not be renamed
public enum NumberFilterType {

    equals,
    notEqual,
    greaterThan,
    greaterThanOrEqual,
    lessThan,
    lessThanOrEqual,
    inRange,
    blank,
    notBlank,

}
