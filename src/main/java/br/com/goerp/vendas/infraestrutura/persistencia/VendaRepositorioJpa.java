package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Cpf;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.Pagamento;
import br.com.goerp.vendas.dominio.Venda;
import br.com.goerp.vendas.dominio.VendaRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class VendaRepositorioJpa implements VendaRepositorio {

    private final VendaJpaRepository jpa;

    @Override
    public Venda salvar(Venda venda) {
        VendaJpa registro = venda.id() == null ? novoRegistro(venda) : buscarRegistro(venda.id());
        registro.setSituacao(venda.situacao());
        registro.setCanceladaEm(venda.canceladaEm());
        registro.setMotivoDoCancelamento(venda.motivoDoCancelamento());
        registro.setCanceladaPor(venda.canceladaPor());
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Venda> buscarPorId(Long id) {
        return jpa.findById(id).map(VendaRepositorioJpa::paraDominio);
    }

    private static VendaJpa novoRegistro(Venda venda) {
        var registro = new VendaJpa();
        registro.setEmpresaId(venda.empresaId());
        registro.setNumero(venda.numero());
        registro.setCaixaId(venda.caixaId());
        registro.setOperadorId(venda.operadorId());
        registro.setClienteId(venda.clienteId());
        registro.setCpfNaNota(venda.cpfNaNota() == null ? null : venda.cpfNaNota().valor());
        registro.setDescontoNaVenda(venda.descontoNaVenda().valor());
        registro.setTotal(venda.total().valor());
        registro.setTroco(venda.troco().valor());
        registro.setMomento(venda.momento());
        venda.itens().forEach(item -> registro.getItens().add(new ItemDeVendaJpa(item.produtoId(), item.codigo(),
                item.descricao(), item.quantidade().valor(), item.precoUnitario().valor(), item.desconto().valor())));
        venda.pagamentos().forEach(pagamento -> registro.getPagamentos().add(
                new PagamentoJpa(pagamento.forma(), pagamento.valor().valor())));
        return registro;
    }

    private VendaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    private static Venda paraDominio(VendaJpa registro) {
        var itens = registro.getItens().stream()
                .map(i -> new ItemDeVenda(i.getProdutoId(), i.getCodigo(), i.getDescricao(),
                        new Quantidade(i.getQuantidade()), Dinheiro.de(i.getPrecoUnitario()), Dinheiro.de(i.getDesconto())))
                .toList();
        var pagamentos = registro.getPagamentos().stream()
                .map(p -> new Pagamento(p.getForma(), Dinheiro.de(p.getValor())))
                .toList();
        var dados = new Venda.DadosDaVenda(registro.getEmpresaId(), registro.getCaixaId(), registro.getOperadorId(),
                registro.getClienteId(), registro.getCpfNaNota() == null ? null : Cpf.de(registro.getCpfNaNota()), itens,
                Dinheiro.de(registro.getDescontoNaVenda()), pagamentos);
        return Venda.reconstituir(registro.getId(), registro.getNumero(), dados, registro.getMomento(),
                registro.getSituacao(), registro.getCanceladaEm(), registro.getMotivoDoCancelamento(),
                registro.getCanceladaPor());
    }
}
