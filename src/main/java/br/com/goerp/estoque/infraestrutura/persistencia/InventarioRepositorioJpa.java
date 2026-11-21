package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioRepositorio;
import br.com.goerp.estoque.dominio.SituacaoDoInventario;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class InventarioRepositorioJpa implements InventarioRepositorio {

    private final InventarioJpaRepository jpa;

    @Override
    public Inventario salvar(Inventario inventario) {
        InventarioJpa registro = inventario.id() == null ? novoRegistro(inventario) : buscarRegistro(inventario);
        registro.setSituacao(inventario.situacao());
        registro.setEncerradoEm(inventario.encerradoEm());
        registro.getContagens().clear();
        inventario.contagens().forEach((produto, contada) ->
                registro.getContagens().add(new ContagemJpa(produto, contada.valor())));
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Inventario> buscarPorId(Long id) {
        return jpa.findById(id).map(InventarioRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeAberto() {
        return jpa.existsBySituacao(SituacaoDoInventario.ABERTO);
    }

    @Override
    public List<Inventario> listar() {
        return jpa.findAllByOrderByIdDesc().stream().map(InventarioRepositorioJpa::paraDominio).toList();
    }

    private InventarioJpa novoRegistro(Inventario inventario) {
        var registro = new InventarioJpa();
        registro.setEmpresaId(inventario.empresaId());
        registro.setDescricao(inventario.descricao());
        registro.setAbertoEm(inventario.abertoEm());
        return registro;
    }

    /** Recusa gravar por cima de uma alteração feita por outra pessoa depois da leitura. */
    private InventarioJpa buscarRegistro(Inventario inventario) {
        InventarioJpa registro = jpa.findById(inventario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inventário", inventario.id()));
        if (!Objects.equals(registro.getVersao(), inventario.versao())) {
            throw new ConflitoDeEdicaoException("Inventário", inventario.id());
        }
        return registro;
    }

    private static Inventario paraDominio(InventarioJpa registro) {
        Map<Long, Quantidade> contagens = new LinkedHashMap<>();
        registro.getContagens().forEach(contagem ->
                contagens.put(contagem.getProdutoId(), new Quantidade(contagem.getQuantidadeContada())));
        return Inventario.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getDescricao(),
                registro.getAbertoEm(), contagens, registro.getSituacao(), registro.getEncerradoEm(),
                registro.getVersao());
    }
}
