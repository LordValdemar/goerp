package br.com.goerp.compartilhado.infraestrutura.seguranca;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Respostas 401 e 403 no mesmo formato JSON (RFC 7807) do restante da API. */
@Component
@RequiredArgsConstructor
public class RespostasDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper json;

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException ex)
            throws IOException {
        escrever(resposta, HttpStatus.UNAUTHORIZED, "Não autenticado", "Token de acesso ausente, inválido ou expirado");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException ex)
            throws IOException {
        escrever(resposta, HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para esta operação");
    }

    private void escrever(HttpServletResponse resposta, HttpStatus status, String titulo, String detalhe)
            throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
