package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class UnidadeDeMedidaRepositorioJpa implements UnidadeDeMedidaRepositorio {

    private final UnidadeDeMedidaJpaRepository jpa;

    @Override
    public UnidadeDeMedida salvar(UnidadeDeMedida unidade) {
        UnidadeDeMedidaJpa registro = unidade.id() == null ? new UnidadeDeMedidaJpa() : buscarRegistro(unidade.id());
        registro.setEmpresaId(unidade.empresaId());
        registro.setSigla(unidade.sigla());
        registro.setDescricao(unidade.descricao());
        registro.setFracionavel(unidade.fracionavel());
        registro.setAtiva(unidade.ativa());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<UnidadeDeMedida> buscarPorId(Long id) {
        return jpa.findById(id).map(UnidadeDeMedidaRepositorioJpa::paraDominio);
    }

    @Override
    public List<UnidadeDeMedida> listar(Boolean ativa) {
        var registros = ativa == null ? jpa.findAllByOrderBySigla() : jpa.findByAtivaOrderBySigla(ativa);
        return registros.stream().map(UnidadeDeMedidaRepositorioJpa::paraDominio).toList();
    }

    @Override
    public boolean existeComSigla(String sigla) {
        return jpa.existsBySigla(sigla);
    }

    private UnidadeDeMedidaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade de medida", id));
    }

    private static UnidadeDeMedida paraDominio(UnidadeDeMedidaJpa registro) {
        return UnidadeDeMedida.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getSigla(),
                registro.getDescricao(), registro.isFracionavel(), registro.isAtiva());
    }
}
