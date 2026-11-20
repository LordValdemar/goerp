package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Duration;
import java.util.Objects;

/** Por quanto tempo um token de renovação continua válido. */
public record PoliticaDeSessao(Duration validadeDaRenovacao) {

    public PoliticaDeSessao {
        Objects.requireNonNull(validadeDaRenovacao, "validadeDaRenovacao");
    }
}
