package br.com.goerp.cadastros.cliente.dominio;

import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.Documento;

/** Clientes da empresa atual (o filtro por empresa é automático). */
public interface ClienteRepositorio {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    boolean existeComDocumento(Documento documento);

    Pagina<Cliente> pesquisar(FiltroDeClientes filtro, Paginacao paginacao);
}
