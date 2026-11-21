package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/**
 * Busca de títulos de uma natureza: um título a receber pedido como "conta a pagar" é tratado como
 * inexistente (404), para cada rota só enxergar o seu lado.
 */
@ServicoDeAplicacao
public class TitulosDaEmpresa {

    private final TituloRepositorio titulos;
    private final FusoHorario fuso;
    private final Clock relogio;

    public TitulosDaEmpresa(TituloRepositorio titulos, FusoHorario fuso, Clock relogio) {
        this.titulos = titulos;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public Titulo buscar(NaturezaDoTitulo natureza, Long id) {
        return titulos.buscarPorId(id).filter(t -> t.natureza() == natureza)
                .orElseThrow(() -> naoEncontrado(natureza, id));
    }

    /**
     * Trava já na primeira leitura: se o título fosse lido antes, a transação seguiria com essa cópia
     * antiga depois de esperar a trava, sem ver o pagamento que a outra acabou de gravar.
     */
    public Titulo travar(NaturezaDoTitulo natureza, Long id) {
        return titulos.travar(id).filter(t -> t.natureza() == natureza).orElseThrow(() -> naoEncontrado(natureza, id));
    }

    public LocalDate hoje() {
        return fuso.hoje(relogio);
    }

    public TituloConsultado consultado(Titulo titulo) {
        LocalDate hoje = hoje();
        return new TituloConsultado(titulo, titulo.vencido(hoje), titulo.diasEmAtraso(hoje));
    }

    private static RecursoNaoEncontradoException naoEncontrado(NaturezaDoTitulo natureza, Long id) {
        return new RecursoNaoEncontradoException(natureza == NaturezaDoTitulo.A_PAGAR ? "Conta a pagar"
                : "Conta a receber", id);
    }
}
