package io.github.codedogapp.gridlink.core.filter;

import java.util.List;


/**
 * A date field filter matching ag-grid's date filter model format.
 * <p>
 * Simple:   {@code { "type": "equals",  "dateFrom": "2024-01-01" }} <br>
 * InRange:  {@code { "type": "inRange", "dateFrom": "2024-01-01", "dateTo": "2024-12-31" }} <br>
 * Compound: {@code { "operator": "AND", "conditions": [...] }} <br>
 * <p>
 * Construct programmatically via {@link #builder()}:
 * <pre>{@code
 * DateFieldFilter.builder()
 *     .type(DateFilterType.inRange)
 *     .dateFrom("2024-01-01")
 *     .dateTo("2024-12-31")
 *     .build();
 * }</pre>
 */
public record DateFieldFilter(
    DateFilterType type,
    String dateFrom,
    String dateTo,
    FilterOperator operator,
    List<DateFieldFilter> conditions
) implements ColumnFilter {

    /**
     * @return a new fluent {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link DateFieldFilter}. Every property is optional and defaults to {@code null}.
     */
    public static final class Builder {

        private DateFilterType type;
        private String dateFrom;
        private String dateTo;
        private FilterOperator operator;
        private List<DateFieldFilter> conditions;

        private Builder() {
        }

        public Builder type(final DateFilterType type) {
            this.type = type;
            return this;
        }

        public Builder dateFrom(final String dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public Builder dateTo(final String dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public Builder operator(final FilterOperator operator) {
            this.operator = operator;
            return this;
        }

        public Builder conditions(final List<DateFieldFilter> conditions) {
            this.conditions = conditions;
            return this;
        }

        public DateFieldFilter build() {
            return new DateFieldFilter(type, dateFrom, dateTo, operator, conditions);
        }

    }

}
