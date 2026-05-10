package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.exception.RedisQueueUnavailableException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.security.JwtAuthenticationFilter;
import com.tcs.backnegocio.service.ArquivoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArquivoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.tcs.backnegocio.exception.GlobalExceptionHandler.class)
class ArquivoControllerProcessarTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArquivoService arquivoService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnAcceptedWhenEnqueueSucceeds() throws Exception {
        mockMvc.perform(post("/arquivos/{id}/processar", 10)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(arquivoService).enqueueForProcessing(10);
    }

    @Test
    void shouldReturnNotFoundWhenArquivoDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("File not found with id: 99"))
                .when(arquivoService)
                .enqueueForProcessing(99);

        mockMvc.perform(post("/arquivos/{id}/processar", 99)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found with id: 99"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenRedisIsDown() throws Exception {
        doThrow(new RedisQueueUnavailableException("Redis queue is unavailable for document processing", new RuntimeException("down")))
                .when(arquivoService)
                .enqueueForProcessing(12);

        mockMvc.perform(post("/arquivos/{id}/processar", 12)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Redis queue is unavailable for document processing"));
    }
}
