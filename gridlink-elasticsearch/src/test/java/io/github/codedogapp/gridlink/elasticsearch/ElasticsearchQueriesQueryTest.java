package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.FieldFilter;
import io.github.codedogapp.gridlink.core.filter.FilterModel;
import io.github.codedogapp.gridlink.core.grid.GridRequest;
import io.github.codedogapp.gridlink.core.sort.SortDirection;
import io.github.codedogapp.gridlink.core.sort.SortModel;
import io.github.codedogapp.gridlink.core.filter.TextFilterType;

import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ElasticsearchQueriesQueryTest {

    private static final FieldFilter NAME = FieldFilter.builder()
        .type(TextFilterType.contains)
        .filter("mac")
        .build();

    @Test
    void assemblesCriteriaSortAndPageFromRequest() {
        final FilterModel model = () -> Map.of("name", NAME);
        final GridRequest<FilterModel> request = new GridRequest<>(
            100,
            200,
            List.of(new SortModel("name", SortDirection.asc)),
            model
        );

        final CriteriaQuery query = ElasticsearchQueries.toQuery(request);

        assertEquals(ElasticsearchQueries.toCriteria(model), query.getCriteria(), "criteria");
        assertEquals(request.pageNumber(), query.getPageable().getPageNumber(), "pageNumber");
        assertEquals(request.limit(), query.getPageable().getPageSize(), "pageSize");

        assertNotNull(query.getSort());
        final Sort.Order order = query.getSort().getOrderFor("name");
        assertNotNull(order, "sort order for name");
        assertTrue(order.isAscending(), "ascending");
    }

    @Test
    void nullFilterModelYieldsMatchAllWithDefaultPageSize() {
        final GridRequest<FilterModel> request = new GridRequest<>(0, 0, List.of(), null);

        final CriteriaQuery query = ElasticsearchQueries.toQuery(request);

        assertEquals(new Criteria(), query.getCriteria(), "match-all criteria");
        assertEquals(0, query.getPageable().getPageNumber(), "pageNumber");
        assertEquals(GridRequest.DEFAULT_PAGE_SIZE, query.getPageable().getPageSize(), "default pageSize");
    }
}
