package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CategoriaRepositorioJpa implements CategoriaRepositorio {

    private final CategoriaJpaRepository jpa;

    @Override
    public Categoria salvar(Categoria categoria) {
        CategoriaJpa registro = categoria.id() == null ? new CategoriaJpa() : buscarRegistro(categoria.id());
        registro.setEmpresaId(categoria.empresaId());
        registro.setNome(categoria.nome());
        registro.setAtiva(categoria.ativa());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return jpa.findById(id).map(CategoriaRepositorioJpa::paraDominio);
    }

    @Override
    public List<Categoria> listar(Boolean ativa) {
        var registros = ativa == null ? jpa.findAllByOrderByNome() : jpa.findByAtivaOrderByNome(ativa);
        return registros.stream().map(CategoriaRepositorioJpa::paraDominio).toList();
    }

    @Override
    public boolean existeComNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existeOutraComNome(String nome, Long idIgnorado) {
        return jpa.existsByNomeIgnoreCaseAndIdNot(nome, idIgnorado);
    }

    private CategoriaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private static Categoria paraDominio(CategoriaJpa registro) {
        return Categoria.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getNome(), registro.isAtiva());
    }
}
