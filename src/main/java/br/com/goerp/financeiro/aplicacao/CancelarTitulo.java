package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/** Cancela (sem apagar) um título sem pagamentos; com pagamento, é preciso estorná-lo antes. */
@CasoDeUso
public class CancelarTitulo {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;
    private final Clock relogio;

    public CancelarTitulo(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa, Clock relogio) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
        this.relogio = relogio;
    }

    public TituloConsultado executar(NaturezaDoTitulo natureza, Long id, String motivo) {
        Titulo titulo = titulosDaEmpresa.travar(natureza, id);
        titulo.cancelar(motivo, relogio.instant());
        return titulosDaEmpresa.consultado(titulos.salvar(titulo));
    }
}
