package io.github.codedogapp.gridlink.elasticsearch;

import io.github.codedogapp.gridlink.core.filter.DateFilterType;
import io.github.codedogapp.gridlink.core.filter.FilterOperator;
import io.github.codedogapp.gridlink.core.sort.SortDirection;
import io.github.codedogapp.gridlink.core.filter.TextFilterType;

import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Translates framework-agnostic GridLink filter tokens into Spring Data Elasticsearch
 * {@link Criteria} / {@link Sort.Direction}.
 * <p>
 * This is the single place that touches Spring types. It only calls the long-stable subset of the
 * {@code Criteria} API ({@code where/is/not/between/expression/contains/exists/greaterThan/lessThan})
 * which is identical across spring-data-elasticsearch 5.x and 6.x, so one artifact serves both
 * Spring Boot 3 and 4.
 */
public final class ElasticsearchCriteria {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ElasticsearchCriteria() {
    }

    /**
     * Builds a {@link Criteria} for a single text filter token.
     *
     * @return the criteria, or {@code null} when the value is blank (except {@code blank}/{@code notBlank}).
     */
    public static @Nullable Criteria text(final TextFilterType type, final String field, final @Nullable String value) {
        if (type == TextFilterType.blank) {
            return Criteria.where(field).not().exists();
        }
        if (type == TextFilterType.notBlank) {
            return Criteria.where(field).exists();
        }
        if (value == null || value.isBlank()) {
            return null;
        }

        final var v = value.toLowerCase();
        return switch (type) {
            case contains -> Criteria.where(field).expression("*" + escape(v) + "*");
            case notContains -> Criteria.where(field).not().contains(v);
            case equals -> Criteria.where(field).is(v);
            case notEqual -> Criteria.where(field).not().is(v);
            case startsWith -> Criteria.where(field).expression(escape(v) + "*");
            case endsWith -> Criteria.where(field).expression("*" + escape(v));
            case blank, notBlank -> throw new IllegalStateException("handled above");
        };
    }

    /**
     * Builds a {@link Criteria} for a single date filter token. For {@code inRange} both bounds are
     * used; every other type uses {@code dateFrom} only.
     *
     * @return the criteria, or {@code null} when the relevant date(s) cannot be parsed.
     */
    public static @Nullable Criteria date(
        final DateFilterType type,
        final String field,
        final @Nullable String dateFrom,
        final @Nullable String dateTo
    ) {
        if (type == DateFilterType.inRange) {
            final var from = parseDate(dateFrom);
            final var to = parseDate(dateTo);
            if (from == null && to == null) {
                return null;
            }
            return Criteria.where(field).between(
                from != null ? toIso(from.atStartOfDay()) : null,
                to != null ? toIso(atEndOfDay(to)) : null
            );
        }

        final var date = parseDate(dateFrom);
        if (date == null) {
            return null;
        }
        return switch (type) {
            case equals -> Criteria.where(field).between(toIso(date.atStartOfDay()), toIso(atEndOfDay(date)));
            case notEqual -> Criteria.where(field).not().between(toIso(date.atStartOfDay()), toIso(atEndOfDay(date)));
            case greaterThan -> Criteria.where(field).greaterThan(toIso(date.atStartOfDay()));
            case lessThan -> Criteria.where(field).lessThan(toIso(date.atStartOfDay()));
            case inRange -> throw new IllegalStateException("handled above");
        };
    }

    /**
     * Combines a list of criteria with the given boolean operator.
     *
     * @return the combined criteria, or {@code null} when the list is empty.
     */
    public static @Nullable Criteria chain(final FilterOperator operator, final List<Criteria> criteria) {
        return switch (operator) {
            case AND -> criteria.stream().reduce(Criteria::and).orElse(null);
            case OR -> criteria.stream().reduce(Criteria::or).orElse(null);
        };
    }

    /**
     * Maps a GridLink {@link SortDirection} to a Spring Data {@link Sort.Direction}.
     */
    public static Sort.Direction direction(final SortDirection direction) {
        return switch (direction) {
            case asc -> Sort.Direction.ASC;
            case desc -> Sort.Direction.DESC;
        };
    }

    private static String escape(final String value) {
        return value.replace(" ", "\\ ");
    }

    private static @Nullable LocalDate parseDate(final @Nullable String value) {
        if (value == null || value.isBlank() || value.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(value.substring(0, 10));
        } catch (final Exception e) {
            return null;
        }
    }

    private static String toIso(final LocalDateTime dateTime) {
        return dateTime.format(ISO);
    }

    private static LocalDateTime atEndOfDay(final LocalDate date) {
        return date.atTime(23, 59, 59);
    }

}
