package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.SaldoDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoqueRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class SaldoDeEstoqueRepositorioJpa implements SaldoDeEstoqueRepositorio {

    private final SaldoDeEstoqueJpaRepository jpa;

    @Override
    public SaldoDeEstoque travarParaMovimentar(Long empresaId, Long produtoId) {
        jpa.criarSeNaoExistir(empresaId, produtoId);
        return jpa.travar(produtoId).map(SaldoDeEstoqueRepositorioJpa::paraDominio).orElseThrow();
    }

    @Override
    public void salvar(SaldoDeEstoque saldo) {
        SaldoDeEstoqueJpa registro = jpa.findByProdutoId(saldo.produtoId()).orElseThrow();
        registro.setQuantidade(saldo.quantidade().valor());
        registro.setCustoMedio(saldo.custoMedio().valor());
    }

    @Override
    public Optional<SaldoDeEstoque> buscar(Long produtoId) {
        return jpa.findByProdutoId(produtoId).map(SaldoDeEstoqueRepositorioJpa::paraDominio);
    }

    private static SaldoDeEstoque paraDominio(SaldoDeEstoqueJpa registro) {
        return SaldoDeEstoque.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getProdutoId(),
                new Quantidade(registro.getQuantidade()), new CustoUnitario(registro.getCustoMedio()));
    }
}
