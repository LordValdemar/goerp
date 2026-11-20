package br.com.goerp.cadastros.cliente.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.cadastros.cliente.dominio.DadosDoCliente;
import br.com.goerp.cadastros.cliente.dominio.FiltroDeClientes;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.EnderecoJpa;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ClienteRepositorioJpa implements ClienteRepositorio {

    private static final Sort ORDEM = Sort.by("nome").and(Sort.by("id"));

    private final ClienteJpaRepository jpa;

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteJpa registro = cliente.id() == null ? novoRegistro(cliente) : buscarRegistro(cliente.id());
        copiarDados(cliente, registro);
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(ClienteRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComDocumento(Documento documento) {
        return jpa.existsByDocumento(documento);
    }

    @Override
    public Pagina<Cliente> pesquisar(FiltroDeClientes filtro, Paginacao paginacao) {
        return Paginas.daSpring(jpa.findAll(PesquisaDeClientes.com(filtro), Paginas.paraSpring(paginacao, ORDEM)),
                ClienteRepositorioJpa::paraDominio);
    }

    private ClienteJpa novoRegistro(Cliente cliente) {
        var registro = new ClienteJpa();
        registro.setEmpresaId(cliente.empresaId());
        registro.setDocumento(cliente.documento());
        return registro;
    }

    private ClienteJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    private static void copiarDados(Cliente cliente, ClienteJpa registro) {
        DadosDoCliente dados = cliente.dados();
        registro.setNome(dados.nome());
        registro.setNomeFantasia(dados.nomeFantasia());
        registro.setInscricaoEstadual(dados.inscricaoEstadual());
        registro.setIndicadorDeIe(dados.indicadorDeIe());
        registro.setEmail(dados.email());
        registro.setTelefone(dados.telefone());
        registro.setEndereco(EnderecoJpa.de(dados.endereco()));
        registro.setLimiteDeCredito(dados.limiteDeCredito());
        registro.setObservacoes(dados.observacoes());
        registro.setAtivo(cliente.ativo());
    }

    private static Cliente paraDominio(ClienteJpa registro) {
        var dados = new DadosDoCliente(registro.getNome(), registro.getNomeFantasia(), registro.getInscricaoEstadual(),
                registro.getIndicadorDeIe(), registro.getEmail(), registro.getTelefone(),
                EnderecoJpa.paraDominio(registro.getEndereco()), registro.getLimiteDeCredito(), registro.getObservacoes());
        return Cliente.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getDocumento(), dados,
                registro.isAtivo(), registro.getVersao());
    }
}
