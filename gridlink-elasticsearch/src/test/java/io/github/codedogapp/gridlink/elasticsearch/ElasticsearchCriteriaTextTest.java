package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.TextFilterType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ElasticsearchCriteriaTextTest {

    static Stream<Arguments> textCases() {
        return Stream.of(
            arguments(TextFilterType.contains, "Ab C", Criteria.where("f").expression("*ab\\ c*")),
            arguments(TextFilterType.notContains, "AbC", Criteria.where("f").not().contains("abc")),
            arguments(TextFilterType.equals, "ABC", Criteria.where("f").is("abc")),
            arguments(TextFilterType.notEqual, "ABC", Criteria.where("f").not().is("abc")),
            arguments(TextFilterType.startsWith, "Ab C", Criteria.where("f").expression("ab\\ c*")),
            arguments(TextFilterType.endsWith, "Ab C", Criteria.where("f").expression("*ab\\ c")),
            arguments(TextFilterType.blank, null, Criteria.where("f").not().exists()),
            arguments(TextFilterType.notBlank, null, Criteria.where("f").exists())
        );
    }

    @ParameterizedTest
    @MethodSource("textCases")
    void buildsExpectedCriteria(final TextFilterType type, final String value, final Criteria expected) {
        assertEquals(expected, ElasticsearchCriteria.text(type, "f", value));
    }

    @ParameterizedTest
    @EnumSource(
        value = TextFilterType.class,
        names = {"contains", "notContains", "equals", "notEqual", "startsWith", "endsWith"}
    )
    void returnsNullForBlankValue(final TextFilterType type) {
        assertNull(ElasticsearchCriteria.text(type, "f", "   "));
    }

}
