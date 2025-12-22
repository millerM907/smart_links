package org.millerM907.rules.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.rules.models.RequestContext;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.millerM907.rules.services.ResolveUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResolveController.class)
@DisplayName("ResolveController — MVC tests")
class ResolveControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ResolveUseCase resolveUseCase;

    private ResolveRequest sampleRequest() {
        RequestContext ctx = new RequestContext(Map.of("browser", "Chrome"));
        return new ResolveRequest("sale1111", ctx);
    }

    @Nested
    @DisplayName("Given a valid resolve request")
    class GivenValidRequest {

        @Test
        @DisplayName("When POST /resolve then 200 OK and JSON body with resolution is returned")
        void givenValidRequest_whenPostResolve_thenOkWithResolutionJson() throws Exception {
            Resolution resolution = new Resolution("http://target", 60, "OK");
            when(resolveUseCase.resolve(any(ResolveRequest.class))).thenReturn(resolution);

            mockMvc.perform(post("/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.targetUrl").value("http://target"))
                    .andExpect(jsonPath("$.ttlSeconds").value(60))
                    .andExpect(jsonPath("$.reason").value("OK"));

            ArgumentCaptor<ResolveRequest> captor = forClass(ResolveRequest.class);
            verify(resolveUseCase).resolve(captor.capture());
            ResolveRequest passed = captor.getValue();
            assertThat(passed.slug()).isEqualTo("sale1111");
            assertThat(passed.context().attrs().get("browser")).isEqualTo("Chrome");
        }
    }

    @Nested
    @DisplayName("Given use case throws business error")
    class GivenUseCaseError {

        @Test
        @DisplayName("When IllegalArgumentException is thrown then 400 Bad Request is returned")
        void givenIllegalArgument_whenPostResolve_thenBadRequest() throws Exception {
            when(resolveUseCase.resolve(any(ResolveRequest.class)))
                    .thenThrow(new IllegalArgumentException("Invalid slug"));

            mockMvc.perform(post("/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest())))
                    .andExpect(status().isBadRequest());
        }
    }
}

