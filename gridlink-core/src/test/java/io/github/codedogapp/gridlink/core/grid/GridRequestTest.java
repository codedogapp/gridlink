package io.github.codedogapp.gridlink.core.grid;

import io.github.codedogapp.gridlink.core.filter.FilterModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridRequestTest {

    private static GridRequest<FilterModel> request(final int startRow, final int endRow) {
        return new GridRequest<>(startRow, endRow, List.of(), null);
    }

    static Stream<Arguments> windows() {
        return Stream.of(
            // startRow, endRow, expectedOffset, expectedLimit, expectedPageNumber
            Arguments.of(0, 100, 0, 100, 0),      // typical first block
            Arguments.of(100, 200, 100, 100, 1),  // typical later block
            Arguments.of(200, 300, 200, 100, 2),  // further block
            Arguments.of(0, 25, 0, 25, 0),        // partial block
            Arguments.of(15, 20, 15, 5, 3),       // block-aligned to its own size
            Arguments.of(-5, 100, 0, 100, 0),     // negative startRow clamps to 0
            Arguments.of(50, 50, 50, GridRequest.DEFAULT_PAGE_SIZE, 0),   // empty window -> default
            Arguments.of(50, 10, 50, GridRequest.DEFAULT_PAGE_SIZE, 0),   // endRow < startRow -> default
            Arguments.of(0, 0, 0, GridRequest.DEFAULT_PAGE_SIZE, 0),      // zero window -> default
            Arguments.of(-10, -1, 0, GridRequest.DEFAULT_PAGE_SIZE, 0)    // both negative -> clamp + default
        );
    }

    @ParameterizedTest
    @MethodSource("windows")
    void derivesOffsetLimitAndPageNumber(final int startRow, final int endRow,
                                         final int expectedOffset, final int expectedLimit,
                                         final int expectedPageNumber) {
        final GridRequest<FilterModel> request = request(startRow, endRow);

        assertEquals(expectedOffset, request.offset(), "offset");
        assertEquals(expectedLimit, request.limit(), "limit");
        assertEquals(expectedPageNumber, request.pageNumber(), "pageNumber");
    }

    @Test
    void rawRowValuesArePreserved() {
        final GridRequest<FilterModel> request = request(-5, 25);

        assertEquals(-5, request.startRow(), "startRow is kept raw for round-tripping");
        assertEquals(25, request.endRow(), "endRow is kept raw for round-tripping");
    }
}
