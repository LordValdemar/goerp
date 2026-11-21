package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial.Serie;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;
import br.com.goerp.vendas.dominio.SituacaoDoPedido;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PedidoDeVendaRepositorioJpa implements PedidoDeVendaRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "numero");

    private final PedidoDeVendaJpaRepository jpa;
    private final ContadorSequencial contador;

    @Override
    public PedidoDeVenda salvar(PedidoDeVenda pedido) {
        PedidoDeVendaJpa registro = pedido.id() == null ? novoRegistro(pedido) : buscarRegistro(pedido);
        var conteudo = pedido.conteudo();
        var andamento = pedido.andamento();
        registro.setDesconto(conteudo.desconto().valor());
        registro.setTotal(pedido.total().valor());
        registro.setValidade(conteudo.validade());
        registro.setObservacoes(conteudo.observacoes());
        registro.setSituacao(andamento.situacao());
        registro.setConfirmadoEm(andamento.confirmadoEm());
        registro.setFaturadoEm(andamento.faturadoEm());
        registro.setCanceladoEm(andamento.canceladoEm());
        registro.setMotivoDoCancelamento(andamento.motivoDoCancelamento());
        registro.getItens().clear();
        conteudo.itens().forEach(item -> registro.getItens().add(new ItemDeVendaJpa(item.produtoId(), item.codigo(),
                item.descricao(), item.quantidade().valor(), item.precoUnitario().valor(), item.desconto().valor())));
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<PedidoDeVenda> buscarPorId(Long id) {
        return jpa.findById(id).map(PedidoDeVendaRepositorioJpa::paraDominio);
    }

    @Override
    public Pagina<PedidoDeVenda> pesquisar(Long clienteId, SituacaoDoPedido situacao, Paginacao paginacao) {
        Specification<PedidoDeVendaJpa> doCliente = (p, c, cb) -> clienteId == null ? null
                : cb.equal(p.get("clienteId"), clienteId);
        Specification<PedidoDeVendaJpa> naSituacao = (p, c, cb) -> situacao == null ? null
                : cb.equal(p.get("situacao"), situacao);
        return Paginas.daSpring(jpa.findAll(Specification.allOf(doCliente, naSituacao),
                Paginas.paraSpring(paginacao, MAIS_RECENTES_PRIMEIRO)), PedidoDeVendaRepositorioJpa::paraDominio);
    }

    @Override
    public long proximoNumero(Long empresaId) {
        return contador.proximo(Serie.PEDIDO_DE_VENDA, empresaId);
    }

    private static PedidoDeVendaJpa novoRegistro(PedidoDeVenda pedido) {
        var registro = new PedidoDeVendaJpa();
        registro.setEmpresaId(pedido.empresaId());
        registro.setNumero(pedido.numero());
        registro.setClienteId(pedido.clienteId());
        registro.setVendedorId(pedido.vendedorId());
        registro.setCriadoNoPedidoEm(pedido.criadoEm());
        return registro;
    }

    private PedidoDeVendaJpa buscarRegistro(PedidoDeVenda pedido) {
        PedidoDeVendaJpa registro = jpa.findById(pedido.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda", pedido.id()));
        if (!Objects.equals(registro.getVersao(), pedido.versao())) {
            throw new ConflitoDeEdicaoException("Pedido de venda", pedido.id());
        }
        return registro;
    }

    private static PedidoDeVenda paraDominio(PedidoDeVendaJpa r) {
        var itens = r.getItens().stream()
                .map(i -> new ItemDeVenda(i.getProdutoId(), i.getCodigo(), i.getDescricao(),
                        new Quantidade(i.getQuantidade()), Dinheiro.de(i.getPrecoUnitario()), Dinheiro.de(i.getDesconto())))
                .toList();
        return PedidoDeVenda.reconstituir(r.getId(), r.getNumero(), r.getEmpresaId(), r.getClienteId(),
                r.getVendedorId(), r.getCriadoNoPedidoEm(),
                new PedidoDeVenda.Conteudo(itens, Dinheiro.de(r.getDesconto()), r.getValidade(), r.getObservacoes()),
                new PedidoDeVenda.Andamento(r.getSituacao(), r.getConfirmadoEm(), r.getFaturadoEm(), r.getCanceladoEm(),
                        r.getMotivoDoCancelamento()),
                r.getVersao());
    }
}
