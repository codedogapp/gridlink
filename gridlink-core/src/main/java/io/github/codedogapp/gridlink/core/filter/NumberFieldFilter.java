package io.github.codedogapp.gridlink.core.filter;

import java.util.List;


/**
 * A number field filter mirroring ag-grid's number filter model JSON, so it binds straight from the
 * wire with no mapping code.
 * <p>
 * Simple:   {@code { "filterType": "number", "type": "greaterThan", "filter": 10 }} <br>
 * InRange:  {@code { "filterType": "number", "type": "inRange", "filter": 10, "filterTo": 20 }} <br>
 * Compound: {@code { "filterType": "number", "operator": "AND", "conditions": [...] }}
 * <p>
 * The {@code filter} / {@code filterTo} values are {@link Number}s; Jackson binds each JSON number to an
 * {@code Integer}, {@code Long} or {@code Double} as appropriate, so integral values stay integral.
 * <p>
 * Construct programmatically via {@link #builder()}:
 * <pre>{@code
 * NumberFieldFilter.builder()
 *     .type(NumberFilterType.inRange)
 *     .filter(10)
 *     .filterTo(20)
 *     .build();
 * }</pre>
 */
public record NumberFieldFilter(
    NumberFilterType type,
    Number filter,
    Number filterTo,
    FilterOperator operator,
    List<NumberFieldFilter> conditions
) implements ColumnFilter {

    /**
     * @return a new fluent {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link NumberFieldFilter}. Every property is optional and defaults to {@code null}.
     */
    public static final class Builder {

        private NumberFilterType type;
        private Number filter;
        private Number filterTo;
        private FilterOperator operator;
        private List<NumberFieldFilter> conditions;

        private Builder() {
        }

        public Builder type(final NumberFilterType type) {
            this.type = type;
            return this;
        }

        public Builder filter(final Number filter) {
            this.filter = filter;
            return this;
        }

        public Builder filterTo(final Number filterTo) {
            this.filterTo = filterTo;
            return this;
        }

        public Builder operator(final FilterOperator operator) {
            this.operator = operator;
            return this;
        }

        public Builder conditions(final List<NumberFieldFilter> conditions) {
            this.conditions = conditions;
            return this;
        }

        public NumberFieldFilter build() {
            return new NumberFieldFilter(type, filter, filterTo, operator, conditions);
        }

    }

}
