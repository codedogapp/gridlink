package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.DateFieldFilter;
import io.github.codedogapp.gridlink.core.filter.DateFilterType;
import io.github.codedogapp.gridlink.core.filter.FieldFilter;
import io.github.codedogapp.gridlink.core.filter.FilterOperator;
import io.github.codedogapp.gridlink.core.sort.SortModel;
import io.github.codedogapp.gridlink.core.sort.SortDirection;
import io.github.codedogapp.gridlink.core.filter.TextFilterType;

import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.List;

import static io.github.codedogapp.gridlink.elasticsearch.CriteriaTestSupport.iso;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ElasticsearchQueriesTest {

    @Test
    void compoundTextFilterChainsConditions() {
        final var filter = FieldFilter.builder()
            .operator(FilterOperator.OR)
            .conditions(List.of(
                FieldFilter.builder().type(TextFilterType.equals).filter("x").build(),
                FieldFilter.builder().type(TextFilterType.equals).filter("y").build()
            ))
            .build();

        assertEquals(
            Criteria.where("f").is("x").or(Criteria.where("f").is("y")),
            ElasticsearchQueries.toCriteria("f", filter)
        );
    }

    @Test
    void compoundDateFilterChainsConditions() {
        final var filter = DateFieldFilter.builder()
            .operator(FilterOperator.AND)
            .conditions(List.of(
                DateFieldFilter.builder().type(DateFilterType.greaterThan).dateFrom("2024-01-01").build(),
                DateFieldFilter.builder().type(DateFilterType.lessThan).dateFrom("2024-12-31").build()
            ))
            .build();

        final var expected = Criteria.where("d").greaterThan(iso("2024-01-01", 0, 0, 0))
            .and(Criteria.where("d").lessThan(iso("2024-12-31", 0, 0, 0)));
        assertEquals(expected, ElasticsearchQueries.toCriteria("d", filter));
    }

    @Test
    void sortsReturnsSpringSortsInOrder() {
        final var sorts = ElasticsearchQueries.sorts(List.of(
            new SortModel("a", SortDirection.asc),
            new SortModel("b", SortDirection.desc)
        ));
        assertEquals(List.of(Sort.by(Sort.Direction.ASC, "a"), Sort.by(Sort.Direction.DESC, "b")), sorts);
    }

    @Test
    void nullInputsAreEmpty() {
        assertNull(ElasticsearchQueries.toCriteria("f", (FieldFilter) null));
        assertNull(ElasticsearchQueries.toCriteria("f", (DateFieldFilter) null));
        assertEquals(List.of(), ElasticsearchQueries.sorts(null));
    }

}
