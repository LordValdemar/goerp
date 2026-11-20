package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.situacao;
import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.texto;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.DadosFiscais;
import br.com.goerp.cadastros.produto.dominio.FiltroDeProdutos;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.dominio.Cest;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ProdutoRepositorioJpa implements ProdutoRepositorio {

    private static final Sort ORDEM = Sort.by("descricao").and(Sort.by("id"));

    private final ProdutoJpaRepository jpa;

    @Override
    public Produto salvar(Produto produto) {
        ProdutoJpa registro = produto.id() == null ? novoRegistro(produto) : buscarRegistro(produto.id());
        copiarDados(produto, registro);
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return jpa.findById(id).map(ProdutoRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComCodigo(String codigo) {
        return jpa.existsByCodigo(codigo);
    }

    @Override
    public boolean codigoDeBarrasEmUso(Gtin codigoDeBarras, Long idIgnorado) {
        return idIgnorado == null ? jpa.existsByCodigoDeBarras(codigoDeBarras)
                : jpa.existsByCodigoDeBarrasAndIdNot(codigoDeBarras, idIgnorado);
    }

    @Override
    public Pagina<Produto> pesquisar(FiltroDeProdutos filtro, Paginacao paginacao) {
        Specification<ProdutoJpa> daCategoria = (produto, consulta, criterios) -> filtro.categoriaId() == null ? null
                : criterios.equal(produto.get("categoriaId"), filtro.categoriaId());
        Specification<ProdutoJpa> pesquisa = Specification.allOf(situacao(filtro.ativo()), daCategoria,
                texto(filtro.texto(), "codigoDeBarras", "descricao", "codigo"));
        return Paginas.daSpring(jpa.findAll(pesquisa, Paginas.paraSpring(paginacao, ORDEM)),
                ProdutoRepositorioJpa::paraDominio);
    }

    private ProdutoJpa novoRegistro(Produto produto) {
        var registro = new ProdutoJpa();
        registro.setEmpresaId(produto.empresaId());
        registro.setCodigo(produto.codigo());
        return registro;
    }

    private ProdutoJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private static void copiarDados(Produto produto, ProdutoJpa registro) {
        DadosDoProduto dados = produto.dados();
        registro.setDescricao(dados.descricao());
        registro.setCodigoDeBarras(dados.codigoDeBarras());
        registro.setCategoriaId(dados.categoriaId());
        registro.setUnidadeId(dados.unidadeId());
        registro.setPrecoDeVenda(dados.precoDeVenda());
        registro.setPrecoDeCusto(dados.precoDeCusto().valor());
        registro.setNcm(dados.fiscais().ncm());
        registro.setCest(dados.fiscais().cest() == null ? null : dados.fiscais().cest().valor());
        registro.setOrigem(dados.fiscais().origem());
        registro.setControlaEstoque(dados.controlaEstoque());
        registro.setEstoqueMinimo(dados.estoqueMinimo());
        registro.setAtivo(produto.ativo());
    }

    private static Produto paraDominio(ProdutoJpa registro) {
        var fiscais = new DadosFiscais(registro.getNcm(), registro.getCest() == null ? null : Cest.de(registro.getCest()),
                registro.getOrigem());
        var dados = new DadosDoProduto(registro.getDescricao(), registro.getCodigoDeBarras(), registro.getCategoriaId(),
                registro.getUnidadeId(), registro.getPrecoDeVenda(), new CustoUnitario(registro.getPrecoDeCusto()),
                fiscais, registro.isControlaEstoque(), registro.getEstoqueMinimo());
        return Produto.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getCodigo(), dados,
                registro.isAtivo(), registro.getVersao());
    }
}
