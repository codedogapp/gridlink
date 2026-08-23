package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.NumberFilterType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.stream.Stream;

import static io.github.codedogapp.gridlink.elasticsearch.CriteriaTestSupport.assertBetween;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ElasticsearchCriteriaNumberTest {

    static Stream<Arguments> singleValueCases() {
        return Stream.of(
            arguments(NumberFilterType.equals, Criteria.where("n").is(10)),
            arguments(NumberFilterType.notEqual, Criteria.where("n").not().is(10)),
            arguments(NumberFilterType.greaterThan, Criteria.where("n").greaterThan(10)),
            arguments(NumberFilterType.greaterThanOrEqual, Criteria.where("n").greaterThanEqual(10)),
            arguments(NumberFilterType.lessThan, Criteria.where("n").lessThan(10)),
            arguments(NumberFilterType.lessThanOrEqual, Criteria.where("n").lessThanEqual(10))
        );
    }

    @ParameterizedTest
    @MethodSource("singleValueCases")
    void singleValue(final NumberFilterType type, final Criteria expected) {
        assertEquals(expected, ElasticsearchCriteria.number(type, "n", 10, null));
    }

    @Test
    void inRangeUsesBothBounds() {
        assertBetween(ElasticsearchCriteria.number(NumberFilterType.inRange, "n", 5, 10), "n", 5, 10, false);
    }

    static Stream<Arguments> existenceCases() {
        return Stream.of(
            arguments(NumberFilterType.blank, Criteria.where("n").not().exists()),
            arguments(NumberFilterType.notBlank, Criteria.where("n").exists())
        );
    }

    @ParameterizedTest
    @MethodSource("existenceCases")
    void existence(final NumberFilterType type, final Criteria expected) {
        assertEquals(expected, ElasticsearchCriteria.number(type, "n", null, null));
    }

    @ParameterizedTest
    @EnumSource(
        value = NumberFilterType.class,
        names = {"equals", "notEqual", "greaterThan", "greaterThanOrEqual", "lessThan", "lessThanOrEqual", "inRange"}
    )
    void returnsNullWhenValueMissing(final NumberFilterType type) {
        assertNull(ElasticsearchCriteria.number(type, "n", null, null));
    }

}
