package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.ColumnFilter;
import io.github.codedogapp.gridlink.core.filter.DateFieldFilter;
import io.github.codedogapp.gridlink.core.filter.DateFilterType;
import io.github.codedogapp.gridlink.core.filter.FieldFilter;
import io.github.codedogapp.gridlink.core.filter.FilterModel;
import io.github.codedogapp.gridlink.core.filter.TextFilterType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElasticsearchQueriesFilterModelTest {

    private static final FieldFilter NAME =
        FieldFilter.builder().type(TextFilterType.contains).filter("mac").build();
    private static final FieldFilter CATEGORY =
        FieldFilter.builder().type(TextFilterType.equals).filter("Electronics").build();
    private static final DateFieldFilter CREATED_AT =
        DateFieldFilter.builder().type(DateFilterType.greaterThan).dateFrom("2024-01-01").build();

    private static Map<String, ColumnFilter> columns(final Object... fieldThenFilter) {
        final Map<String, ColumnFilter> map = new LinkedHashMap<>();
        for (int i = 0; i < fieldThenFilter.length; i += 2) {
            map.put((String) fieldThenFilter[i], (ColumnFilter) fieldThenFilter[i + 1]);
        }
        return map;
    }

    static Stream<Arguments> filterMaps() {
        return Stream.of(
            Arguments.of("empty map -> match-all root",
                columns(),
                new Criteria()),
            Arguments.of("single text column keyed by field name",
                columns("name", NAME),
                new Criteria().subCriteria(ElasticsearchQueries.toCriteria("name", NAME))),
            Arguments.of("null column value skipped",
                columns("name", NAME, "category", null),
                new Criteria().subCriteria(ElasticsearchQueries.toCriteria("name", NAME))),
            Arguments.of("every column active, kept in map iteration order",
                columns("name", NAME, "category", CATEGORY, "createdAt", CREATED_AT),
                new Criteria()
                    .subCriteria(ElasticsearchQueries.toCriteria("name", NAME))
                    .subCriteria(ElasticsearchQueries.toCriteria("category", CATEGORY))
                    .subCriteria(ElasticsearchQueries.toCriteria("createdAt", CREATED_AT))),
            Arguments.of("field name is decoupled from any component name",
                columns("productName", NAME),
                new Criteria().subCriteria(ElasticsearchQueries.toCriteria("productName", NAME)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filterMaps")
    void mapBuildsRootBySubCriteriaPerColumn(
        final String name, final Map<String, ColumnFilter> filters, final Criteria expected) {
        assertEquals(expected, ElasticsearchQueries.toCriteria(filters));
    }

    @Test
    void nullMapIsEmptyMatchAll() {
        assertEquals(new Criteria(), ElasticsearchQueries.toCriteria((Map<String, ColumnFilter>) null));
    }

    @Test
    void filterModelDelegatesToItsFiltersMap() {
        final FilterModel model = () -> columns("name", NAME, "createdAt", CREATED_AT);
        final Criteria expected = new Criteria()
            .subCriteria(ElasticsearchQueries.toCriteria("name", NAME))
            .subCriteria(ElasticsearchQueries.toCriteria("createdAt", CREATED_AT));
        assertEquals(expected, ElasticsearchQueries.toCriteria(model));
    }

    @Test
    void nullFilterModelIsEmptyMatchAll() {
        assertEquals(new Criteria(), ElasticsearchQueries.toCriteria((FilterModel) null));
    }

}
