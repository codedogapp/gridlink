package io.github.codedogapp.gridlink.core.filter;


/**
 * ag-grid compatible text filter types, matching ag-grid's text filter model format.
 */
public enum TextFilterType {

    contains,
    notContains,
    equals,
    notEqual,
    startsWith,
    endsWith,
    blank,
    notBlank,

}
