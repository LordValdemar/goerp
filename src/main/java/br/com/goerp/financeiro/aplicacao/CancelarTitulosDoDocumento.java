package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;
import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/**
 * Cancela os títulos gerados por um documento que foi desfeito (recebimento de compra estornado,
 * venda cancelada). Se algum já tiver pagamento, recusa: o pagamento precisa ser estornado antes,
 * e a operação de quem chamou é desfeita inteira.
 */
@CasoDeUso
public class CancelarTitulosDoDocumento {

    private final TituloRepositorio titulos;
    private final Clock relogio;

    public CancelarTitulosDoDocumento(TituloRepositorio titulos, Clock relogio) {
        this.titulos = titulos;
        this.relogio = relogio;
    }

    /** Os já cancelados (à mão) ficam como estão. */
    public List<Titulo> executar(OrigemDoTitulo origem, Long documentoDeOrigem, String motivo) {
        return titulos.doDocumento(origem, documentoDeOrigem).stream()
                .filter(titulo -> titulo.situacao() != SituacaoDoTitulo.CANCELADO)
                .map(titulo -> cancelar(titulo.id(), motivo))
                .toList();
    }

    /** Travado e relido: um pagamento simultâneo é visto (e impede o cancelamento). */
    private Titulo cancelar(Long id, String motivo) {
        Titulo titulo = titulos.travar(id).orElseThrow();
        titulo.cancelar(motivo, relogio.instant());
        return titulos.salvar(titulo);
    }
}
