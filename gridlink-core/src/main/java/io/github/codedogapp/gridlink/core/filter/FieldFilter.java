package io.github.codedogapp.gridlink.core.filter;

import java.util.List;


/**
 * A text field filter mirroring ag-grid's text filter model JSON, so it binds straight from the wire
 * with no mapping code.
 * <p>
 * Simple:   {@code { "filterType": "text", "type": "contains", "filter": "ABC" }} <br>
 * Compound: {@code { "filterType": "text", "operator": "AND", "conditions": [...] }}
 * <p>
 * Construct programmatically via {@link #builder()}:
 * <pre>{@code
 * FieldFilter.builder()
 *     .type(TextFilterType.contains)
 *     .filter("ABC")
 *     .build();
 * }</pre>
 */
public record FieldFilter(
    TextFilterType type,
    String filter,
    FilterOperator operator,
    List<FieldFilter> conditions
) implements ColumnFilter {

    /**
     * @return a new fluent {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link FieldFilter}. Every property is optional and defaults to {@code null}.
     */
    public static final class Builder {

        private TextFilterType type;
        private String filter;
        private FilterOperator operator;
        private List<FieldFilter> conditions;

        private Builder() {
        }

        public Builder type(final TextFilterType type) {
            this.type = type;
            return this;
        }

        public Builder filter(final String filter) {
            this.filter = filter;
            return this;
        }

        public Builder operator(final FilterOperator operator) {
            this.operator = operator;
            return this;
        }

        public Builder conditions(final List<FieldFilter> conditions) {
            this.conditions = conditions;
            return this;
        }

        public FieldFilter build() {
            return new FieldFilter(type, filter, operator, conditions);
        }

    }

}
