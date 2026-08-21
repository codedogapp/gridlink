package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.FilterOperator;
import io.github.codedogapp.gridlink.core.sort.SortDirection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ElasticsearchCriteriaChainTest {

    static Stream<Arguments> chainCases() {
        return Stream.of(
            arguments(
                FilterOperator.AND,
                List.of(Criteria.where("a").is("1"), Criteria.where("b").is("2")),
                Criteria.where("a").is("1").and(Criteria.where("b").is("2"))
            ),
            arguments(
                FilterOperator.OR,
                List.of(Criteria.where("a").is("1"), Criteria.where("b").is("2")),
                Criteria.where("a").is("1").or(Criteria.where("b").is("2"))
            )
        );
    }

    @ParameterizedTest
    @MethodSource("chainCases")
    void chainCombinesWithOperator(final FilterOperator op, final List<Criteria> input, final Criteria expected) {
        assertEquals(expected, ElasticsearchCriteria.chain(op, input));
    }

    @ParameterizedTest
    @EnumSource(FilterOperator.class)
    void chainOfEmptyIsNull(final FilterOperator op) {
        assertNull(ElasticsearchCriteria.chain(op, List.of()));
    }

    @ParameterizedTest
    @CsvSource({"asc, ASC", "desc, DESC"})
    void directionMapsToSpring(final SortDirection in, final Sort.Direction expected) {
        assertEquals(expected, ElasticsearchCriteria.direction(in));
    }

}
