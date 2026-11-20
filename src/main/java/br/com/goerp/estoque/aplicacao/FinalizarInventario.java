package br.com.goerp.estoque.aplicacao;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioRepositorio;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;

/**
 * Encerra o inventário e ajusta o estoque de cada produto contado para a quantidade contada.
 * Tudo numa transação: ou todos os ajustes são lançados e o inventário fica finalizado, ou nada muda.
 */
@CasoDeUso
public class FinalizarInventario {

    private final InventarioRepositorio inventarios;
    private final BuscarInventario buscarInventario;
    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public FinalizarInventario(InventarioRepositorio inventarios, BuscarInventario buscarInventario,
            MovimentadorDeEstoque movimentador, UsuarioAtual usuarioAtual, Clock relogio) {
        this.inventarios = inventarios;
        this.buscarInventario = buscarInventario;
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public ResultadoDoInventario executar(Long inventarioId) {
        Inventario inventario = buscarInventario.executar(inventarioId);
        Map<Long, Quantidade> contagens = inventario.finalizar(relogio.instant());
        String motivo = "Inventário #" + inventario.id() + " - " + inventario.descricao();

        List<Movimentacao> ajustes = new ArrayList<>();
        contagens.forEach((produtoId, contada) -> movimentador.ajustarPara(produtoId, contada, motivo,
                OrigemDaMovimentacao.INVENTARIO, inventario.id(), usuarioAtual.id()).ifPresent(ajustes::add));

        return new ResultadoDoInventario(inventarios.salvar(inventario), ajustes);
    }
}
