package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.DateFilterType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.stream.Stream;

import static io.github.codedogapp.gridlink.elasticsearch.CriteriaTestSupport.assertBetween;
import static io.github.codedogapp.gridlink.elasticsearch.CriteriaTestSupport.iso;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ElasticsearchCriteriaDateTest {

    static Stream<Arguments> singleBoundCases() {
        return Stream.of(
            arguments(DateFilterType.greaterThan, Criteria.where("d").greaterThan(iso("2024-06-15", 0, 0, 0))),
            arguments(DateFilterType.lessThan, Criteria.where("d").lessThan(iso("2024-06-15", 0, 0, 0)))
        );
    }

    @ParameterizedTest
    @MethodSource("singleBoundCases")
    void singleBound(final DateFilterType type, final Criteria expected) {
        assertEquals(expected, ElasticsearchCriteria.date(type, "d", "2024-06-15", null));
    }

    static Stream<Arguments> betweenCases() {
        return Stream.of(
            arguments(DateFilterType.equals, "2024-06-15", null,
                iso("2024-06-15", 0, 0, 0), iso("2024-06-15", 23, 59, 59), false),
            arguments(DateFilterType.notEqual, "2024-06-15", null,
                iso("2024-06-15", 0, 0, 0), iso("2024-06-15", 23, 59, 59), true),
            arguments(DateFilterType.inRange, "2024-01-01", "2024-12-31",
                iso("2024-01-01", 0, 0, 0), iso("2024-12-31", 23, 59, 59), false)
        );
    }

    @ParameterizedTest
    @MethodSource("betweenCases")
    void between(
        final DateFilterType type,
        final String from,
        final String to,
        final String lo,
        final String hi,
        final boolean negating
    ) {
        assertBetween(ElasticsearchCriteria.date(type, "d", from, to), "d", lo, hi, negating);
    }

    @ParameterizedTest
    @EnumSource(DateFilterType.class)
    void returnsNullWhenUnparseable(final DateFilterType type) {
        assertNull(ElasticsearchCriteria.date(type, "d", "nope", "nope"));
    }

}
