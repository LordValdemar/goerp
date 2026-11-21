package br.com.goerp.cadastros.fornecedor.infraestrutura.persistencia;

import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.situacao;
import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.texto;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FiltroDeFornecedores;
import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.EnderecoJpa;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class FornecedorRepositorioJpa implements FornecedorRepositorio {

    private static final Sort ORDEM = Sort.by("nome").and(Sort.by("id"));

    private final FornecedorJpaRepository jpa;

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        FornecedorJpa registro = fornecedor.id() == null ? novoRegistro(fornecedor) : buscarRegistro(fornecedor.id());
        copiarDados(fornecedor, registro);
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Fornecedor> buscarPorId(Long id) {
        return jpa.findById(id).map(FornecedorRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComDocumento(Documento documento) {
        return jpa.existsByDocumento(documento);
    }

    @Override
    public Pagina<Fornecedor> pesquisar(FiltroDeFornecedores filtro, Paginacao paginacao) {
        Specification<FornecedorJpa> pesquisa = Specification.allOf(situacao(filtro.ativo()),
                texto(filtro.texto(), "documento", "nome", "nomeFantasia", "pessoaDeContato"));
        return Paginas.daSpring(jpa.findAll(pesquisa, Paginas.paraSpring(paginacao, ORDEM)),
                FornecedorRepositorioJpa::paraDominio);
    }

    private FornecedorJpa novoRegistro(Fornecedor fornecedor) {
        var registro = new FornecedorJpa();
        registro.setEmpresaId(fornecedor.empresaId());
        registro.setDocumento(fornecedor.documento());
        return registro;
    }

    private FornecedorJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor", id));
    }

    private static void copiarDados(Fornecedor fornecedor, FornecedorJpa registro) {
        DadosDoFornecedor dados = fornecedor.dados();
        registro.setNome(dados.nome());
        registro.setNomeFantasia(dados.nomeFantasia());
        registro.setInscricaoEstadual(dados.inscricaoEstadual());
        registro.setEmail(dados.email());
        registro.setTelefone(dados.telefone());
        registro.setPessoaDeContato(dados.pessoaDeContato());
        registro.setEndereco(EnderecoJpa.de(dados.endereco()));
        registro.setObservacoes(dados.observacoes());
        registro.setAtivo(fornecedor.ativo());
    }

    private static Fornecedor paraDominio(FornecedorJpa registro) {
        var dados = new DadosDoFornecedor(registro.getNome(), registro.getNomeFantasia(), registro.getInscricaoEstadual(),
                registro.getEmail(), registro.getTelefone(), registro.getPessoaDeContato(),
                EnderecoJpa.paraDominio(registro.getEndereco()), registro.getObservacoes());
        return Fornecedor.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getDocumento(), dados,
                registro.isAtivo(), registro.getVersao());
    }
}
