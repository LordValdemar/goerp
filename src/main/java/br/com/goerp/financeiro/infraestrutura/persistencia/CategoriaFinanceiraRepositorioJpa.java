package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CategoriaFinanceiraRepositorioJpa implements CategoriaFinanceiraRepositorio {

    private final CategoriaFinanceiraJpaRepository jpa;

    @Override
    public CategoriaFinanceira salvar(CategoriaFinanceira categoria) {
        CategoriaFinanceiraJpa registro;
        if (categoria.id() == null) {
            registro = new CategoriaFinanceiraJpa();
            registro.setEmpresaId(categoria.empresaId());
            registro.setTipo(categoria.tipo());
        } else {
            registro = jpa.findById(categoria.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria financeira", categoria.id()));
        }
        registro.setNome(categoria.nome());
        registro.setAtiva(categoria.ativa());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<CategoriaFinanceira> buscarPorId(Long id) {
        return jpa.findById(id).map(CategoriaFinanceiraRepositorioJpa::paraDominio);
    }

    @Override
    public List<CategoriaFinanceira> listar(TipoDeCategoria tipo, Boolean ativa) {
        Specification<CategoriaFinanceiraJpa> doTipo = (c, q, cb) -> tipo == null ? null : cb.equal(c.get("tipo"), tipo);
        Specification<CategoriaFinanceiraJpa> naSituacao = (c, q, cb) -> ativa == null ? null
                : cb.equal(c.get("ativa"), ativa);
        return jpa.findAll(Specification.allOf(doTipo, naSituacao), Sort.by("tipo", "nome")).stream()
                .map(CategoriaFinanceiraRepositorioJpa::paraDominio)
                .toList();
    }

    @Override
    public boolean existeComNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existeOutraComNome(String nome, Long idIgnorado) {
        return jpa.existsByNomeIgnoreCaseAndIdNot(nome, idIgnorado);
    }

    private static CategoriaFinanceira paraDominio(CategoriaFinanceiraJpa registro) {
        return CategoriaFinanceira.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getTipo(),
                registro.getNome(), registro.isAtiva());
    }
}
