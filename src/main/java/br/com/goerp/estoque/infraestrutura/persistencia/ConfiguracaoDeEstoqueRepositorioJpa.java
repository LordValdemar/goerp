package br.com.goerp.estoque.infraestrutura.persistencia;

import org.springframework.stereotype.Repository;

import br.com.goerp.estoque.dominio.ConfiguracaoDeEstoqueRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ConfiguracaoDeEstoqueRepositorioJpa implements ConfiguracaoDeEstoqueRepositorio {

    private final ConfiguracaoDeEstoqueJpaRepository jpa;

    @Override
    public boolean permiteEstoqueNegativo() {
        return jpa.findFirstBy().map(ConfiguracaoDeEstoqueJpa::isPermiteEstoqueNegativo).orElse(false);
    }

    @Override
    public void definirPermiteEstoqueNegativo(Long empresaId, boolean permite) {
        ConfiguracaoDeEstoqueJpa registro = jpa.findFirstBy().orElseGet(() -> {
            var nova = new ConfiguracaoDeEstoqueJpa();
            nova.setEmpresaId(empresaId);
            return nova;
        });
        registro.setPermiteEstoqueNegativo(permite);
        jpa.save(registro);
    }
}
