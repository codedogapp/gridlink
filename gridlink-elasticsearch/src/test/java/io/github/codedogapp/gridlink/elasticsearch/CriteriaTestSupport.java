package io.github.codedogapp.gridlink.elasticsearch;

import org.springframework.data.elasticsearch.core.query.Criteria;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Shared assertion/format helpers for the adapter tests.
 */
final class CriteriaTestSupport {

    private CriteriaTestSupport() {
    }

    static String iso(final String date, final int h, final int m, final int s) {
        return LocalDate.parse(date).atTime(h, m, s).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Criteria.between(..) stores an Object[]; CriteriaEntry#equals compares arrays by identity,
    // so equal-content BETWEEN criteria are never .equals — assert the array contents directly.
    static void assertBetween(
        final Criteria actual,
        final String field,
        final Object lo,
        final Object hi,
        final boolean negating
    ) {
        assertNotNull(actual);
        assertEquals(field, actual.getField().getName());
        assertEquals(negating, actual.isNegating());
        final var entries = actual.getQueryCriteriaEntries();
        assertEquals(1, entries.size());
        final var entry = entries.iterator().next();
        assertEquals(Criteria.OperationKey.BETWEEN, entry.getKey());
        assertArrayEquals(new Object[] {lo, hi}, (Object[]) entry.getValue());
    }

}
