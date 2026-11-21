package br.com.goerp.compras.infraestrutura.persistencia;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial.Serie;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.compras.dominio.ItemDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;
import br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PedidoDeCompraRepositorioJpa implements PedidoDeCompraRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "numero");

    private final PedidoDeCompraJpaRepository jpa;
    private final ContadorSequencial contador;

    @Override
    public PedidoDeCompra salvar(PedidoDeCompra pedido) {
        PedidoDeCompraJpa registro = pedido.id() == null ? novoRegistro(pedido) : buscarRegistro(pedido);
        var conteudo = pedido.conteudo();
        var andamento = pedido.andamento();
        registro.setPrevisaoDeEntrega(conteudo.previsaoDeEntrega());
        registro.setObservacoes(conteudo.observacoes());
        registro.setTotal(pedido.total().valor());
        registro.setSituacao(andamento.situacao());
        registro.setEnviadoEm(andamento.enviadoEm());
        registro.setEncerradoEm(andamento.encerradoEm());
        registro.setMotivoDoEncerramento(andamento.motivoDoEncerramento());
        registro.getItens().clear();
        conteudo.itens().forEach(item -> registro.getItens().add(new ItemDeCompraJpa(item.produtoId(), item.codigo(),
                item.descricao(), item.quantidade().valor(), item.custoNegociado().valor(), item.recebida().valor())));
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<PedidoDeCompra> buscarPorId(Long id) {
        return jpa.findById(id).map(PedidoDeCompraRepositorioJpa::paraDominio);
    }

    @Override
    public Pagina<PedidoDeCompra> pesquisar(Long fornecedorId, SituacaoDoPedidoDeCompra situacao, Paginacao paginacao) {
        Specification<PedidoDeCompraJpa> doFornecedor = (p, c, cb) -> fornecedorId == null ? null
                : cb.equal(p.get("fornecedorId"), fornecedorId);
        Specification<PedidoDeCompraJpa> naSituacao = (p, c, cb) -> situacao == null ? null
                : cb.equal(p.get("situacao"), situacao);
        return Paginas.daSpring(jpa.findAll(Specification.allOf(doFornecedor, naSituacao),
                Paginas.paraSpring(paginacao, MAIS_RECENTES_PRIMEIRO)), PedidoDeCompraRepositorioJpa::paraDominio);
    }

    @Override
    public long proximoNumero(Long empresaId) {
        return contador.proximo(Serie.PEDIDO_DE_COMPRA, empresaId);
    }

    @Override
    public List<PedidoDeCompra> aguardandoEntrega(Long fornecedorId) {
        Specification<PedidoDeCompraJpa> doFornecedor = (p, c, cb) -> fornecedorId == null ? null
                : cb.equal(p.get("fornecedorId"), fornecedorId);
        Specification<PedidoDeCompraJpa> aguardando = (p, c, cb) -> p.get("situacao").in(
                SituacaoDoPedidoDeCompra.ENVIADO, SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO);
        // no PostgreSQL, ASC já põe os sem previsão por último (o Hibernate não aceita nullsLast em Criteria)
        Sort porPrevisao = Sort.by(Sort.Order.asc("previsaoDeEntrega"), Sort.Order.asc("numero"));
        return jpa.findAll(Specification.allOf(doFornecedor, aguardando), porPrevisao).stream()
                .map(PedidoDeCompraRepositorioJpa::paraDominio)
                .toList();
    }

    private static PedidoDeCompraJpa novoRegistro(PedidoDeCompra pedido) {
        var registro = new PedidoDeCompraJpa();
        registro.setEmpresaId(pedido.empresaId());
        registro.setNumero(pedido.numero());
        registro.setFornecedorId(pedido.fornecedorId());
        registro.setCompradorId(pedido.compradorId());
        registro.setCriadoNoPedidoEm(pedido.criadoEm());
        return registro;
    }

    /** Recusa gravar por cima de uma alteração (ou recebimento) feita por outra pessoa depois da leitura. */
    private PedidoDeCompraJpa buscarRegistro(PedidoDeCompra pedido) {
        PedidoDeCompraJpa registro = jpa.findById(pedido.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra", pedido.id()));
        if (!Objects.equals(registro.getVersao(), pedido.versao())) {
            throw new ConflitoDeEdicaoException("Pedido de compra", pedido.id());
        }
        return registro;
    }

    private static PedidoDeCompra paraDominio(PedidoDeCompraJpa r) {
        var itens = r.getItens().stream()
                .map(i -> new ItemDeCompra(i.getProdutoId(), i.getCodigo(), i.getDescricao(),
                        new Quantidade(i.getQuantidade()), new CustoUnitario(i.getCustoNegociado()),
                        new Quantidade(i.getRecebida())))
                .toList();
        return PedidoDeCompra.reconstituir(r.getId(), r.getNumero(), r.getEmpresaId(), r.getFornecedorId(),
                r.getCompradorId(), r.getCriadoNoPedidoEm(),
                new PedidoDeCompra.Conteudo(itens, r.getPrevisaoDeEntrega(), r.getObservacoes()),
                new PedidoDeCompra.Andamento(r.getSituacao(), r.getEnviadoEm(), r.getEncerradoEm(),
                        r.getMotivoDoEncerramento()),
                r.getVersao());
    }
}
