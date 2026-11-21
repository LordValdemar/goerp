package br.com.goerp.vendas.infraestrutura.persistencia;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial.Serie;
import br.com.goerp.vendas.dominio.NumeracaoDeVendas;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class NumeracaoDeVendasJpa implements NumeracaoDeVendas {

    private final ContadorSequencial contador;

    @Override
    public long proximo(Long empresaId) {
        return contador.proximo(Serie.VENDA, empresaId);
    }
}
