package br.com.goerp.compras.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compras.dominio.ItemRecebido;
import br.com.goerp.compras.dominio.Recebimento;
import br.com.goerp.compras.dominio.RecebimentoRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class RecebimentoRepositorioJpa implements RecebimentoRepositorio {

    private final RecebimentoJpaRepository jpa;

    @Override
    public Recebimento salvar(Recebimento recebimento) {
        RecebimentoJpa registro = recebimento.id() == null ? novoRegistro(recebimento)
                : jpa.findById(recebimento.id())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Recebimento", recebimento.id()));
        registro.setEstornado(recebimento.estornado());
        registro.setEstornadoEm(recebimento.estornadoEm());
        registro.setMotivoDoEstorno(recebimento.motivoDoEstorno());
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Recebimento> buscarPorId(Long id) {
        return jpa.findById(id).map(RecebimentoRepositorioJpa::paraDominio);
    }

    @Override
    public List<Recebimento> doPedido(Long pedidoId) {
        return jpa.findByPedidoIdOrderById(pedidoId).stream().map(RecebimentoRepositorioJpa::paraDominio).toList();
    }

    private static RecebimentoJpa novoRegistro(Recebimento recebimento) {
        var registro = new RecebimentoJpa();
        registro.setEmpresaId(recebimento.empresaId());
        registro.setPedidoId(recebimento.pedidoId());
        registro.setNotaFiscal(recebimento.notaFiscal());
        registro.setValorTotal(recebimento.valorTotal().valor());
        registro.setRecebidoPor(recebimento.recebidoPor());
        registro.setRecebidoEm(recebimento.recebidoEm());
        recebimento.itens().forEach(item -> registro.getItens().add(new ItemRecebidoJpa(item.produtoId(),
                item.quantidade().valor(), item.custoUnitario().valor())));
        return registro;
    }

    private static Recebimento paraDominio(RecebimentoJpa r) {
        var itens = r.getItens().stream()
                .map(i -> new ItemRecebido(i.getProdutoId(), new Quantidade(i.getQuantidade()),
                        new CustoUnitario(i.getCustoUnitario())))
                .toList();
        return Recebimento.reconstituir(r.getId(), r.getEmpresaId(), r.getPedidoId(), r.getNotaFiscal(), itens,
                r.getRecebidoPor(), r.getRecebidoEm(), r.isEstornado(), r.getEstornadoEm(), r.getMotivoDoEstorno());
    }
}
