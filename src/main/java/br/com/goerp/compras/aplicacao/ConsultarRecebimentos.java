package br.com.goerp.compras.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compras.dominio.Recebimento;
import br.com.goerp.compras.dominio.RecebimentoRepositorio;

@CasoDeUso
public class ConsultarRecebimentos {

    private final RecebimentoRepositorio recebimentos;
    private final ComprasDaEmpresa compras;

    public ConsultarRecebimentos(RecebimentoRepositorio recebimentos, ComprasDaEmpresa compras) {
        this.recebimentos = recebimentos;
        this.compras = compras;
    }

    public Recebimento porId(Long id) {
        return recebimentos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Recebimento", id));
    }

    public List<Recebimento> doPedido(Long pedidoId) {
        compras.buscar(pedidoId);
        return recebimentos.doPedido(pedidoId);
    }
}
