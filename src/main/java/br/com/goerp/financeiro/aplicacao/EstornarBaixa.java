package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.financeiro.dominio.Baixa;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/**
 * Desfaz um pagamento (cheque devolvido, pagamento lançado no título errado): devolve o saldo
 * devedor e estorna o lançamento na conta, juntos.
 */
@CasoDeUso
public class EstornarBaixa {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;
    private final LancamentoRepositorio lancamentos;
    private final LancadorFinanceiro lancador;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public EstornarBaixa(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa, LancamentoRepositorio lancamentos,
            LancadorFinanceiro lancador, UsuarioAtual usuarioAtual, Clock relogio) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
        this.lancamentos = lancamentos;
        this.lancador = lancador;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public TituloConsultado executar(NaturezaDoTitulo natureza, Long id, int numeroDaBaixa, String motivo) {
        Titulo titulo = titulosDaEmpresa.travar(natureza, id);
        Baixa estornada = titulo.estornarBaixa(numeroDaBaixa, motivo, relogio.instant());
        Titulo salvo = titulos.salvar(titulo);
        lancamentos.doDocumento(OrigemDoLancamento.BAIXA_DE_TITULO, estornada.id()).forEach(original ->
                lancador.lancar(original.contaId(), original.pedidoDeEstorno(estornada.motivoDoEstorno(),
                        usuarioAtual.id())));
        return titulosDaEmpresa.consultado(salvo);
    }
}
