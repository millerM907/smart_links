package org.millerM907.landing.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ErrorHandler — unit tests")
class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Nested
    @DisplayName("Given IllegalArgumentException")
    class GivenIllegalArgumentException {

        @Test
        @DisplayName("When message is non-empty then 400 is returned with error body")
        void givenIllegalArgument_whenNonEmptyMessage_thenBadRequestWithBody() {
            IllegalArgumentException ex = new IllegalArgumentException("Wrong slug");
            ResponseEntity<Map<String, String>> response = handler.bad(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .isNotNull()
                    .containsEntry("error", "Wrong slug");
        }

        @Test
        @DisplayName("When message is empty then 400 is returned with empty error value")
        void givenIllegalArgument_whenEmptyMessage_thenBadRequestWithEmptyError() {
            IllegalArgumentException ex = new IllegalArgumentException("");
            ResponseEntity<Map<String, String>> response = handler.bad(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .isNotNull()
                    .containsEntry("error", "");
        }

        @Test
        @DisplayName("When message is null then Map.of throws NullPointerException")
        void givenIllegalArgument_whenNullMessage_thenNpeFromMapOf() {
            IllegalArgumentException ex = new IllegalArgumentException((String) null);

            assertThatThrownBy(() -> handler.bad(ex))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Given IllegalStateException")
    class GivenIllegalStateException {

        @Test
        @DisplayName("When message is non-empty then 500 is returned with error body")
        void givenIllegalState_whenNonEmptyMessage_thenInternalServerErrorWithBody() {
            IllegalStateException ex = new IllegalStateException("DSL load failed");
            ResponseEntity<Map<String, String>> response = handler.err(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody())
                    .isNotNull()
                    .containsEntry("error", "DSL load failed");
        }

        @Test
        @DisplayName("When message is empty then 500 is returned with empty error value")
        void givenIllegalState_whenEmptyMessage_thenInternalServerErrorWithEmptyError() {
            IllegalStateException ex = new IllegalStateException("");
            ResponseEntity<Map<String, String>> response = handler.err(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody())
                    .isNotNull()
                    .containsEntry("error", "");
        }

        @Test
        @DisplayName("When message is null then Map.of throws NullPointerException")
        void givenIllegalState_whenNullMessage_thenNpeFromMapOf() {
            IllegalStateException ex = new IllegalStateException((String) null);

            assertThatThrownBy(() -> handler.err(ex))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
