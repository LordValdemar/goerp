package br.com.goerp.compartilhado.web;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

/** Converte exceções em respostas JSON padronizadas (RFC 7807). */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail recursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ExcecaoDeDominio.class)
    public ProblemDetail regraDeNegocioViolada(ExcecaoDeDominio ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(ConflitoDeEdicaoException.class)
    public ProblemDetail conflitoDeEdicao(ConflitoDeEdicaoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito de edição", ex.getMessage());
    }

    /** Duas gravações simultâneas do mesmo registro: a segunda perde, em vez de sobrescrever a primeira. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail gravacaoSimultanea(ObjectOptimisticLockingFailureException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito de edição",
                "O registro foi alterado por outra pessoa ao mesmo tempo; recarregue e tente de novo");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail dadosInvalidos(MethodArgumentNotValidException ex) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Erro de validação", "Dados inválidos");
        problema.setProperty("campos", errosPorCampo(ex));
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoIlegivel(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O corpo da requisição não pôde ser lido (JSON malformado ou valor inválido)");
    }

    private static Map<String, String> errosPorCampo(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        erro -> String.valueOf(erro.getDefaultMessage()),
                        (primeiro, segundo) -> primeiro));
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
