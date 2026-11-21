package br.com.goerp.cadastros.cliente.aplicacao;

import java.util.Objects;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.cadastros.cliente.dominio.DadosDoCliente;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;

/**
 * Recebe a versão que o usuário leu: se o cliente mudou desde então, recusa (409) em vez de
 * sobrescrever a alteração de outra pessoa.
 */
@CasoDeUso
public class AlterarCliente {

    private final ClienteRepositorio clientes;
    private final BuscarCliente buscarCliente;

    public AlterarCliente(ClienteRepositorio clientes, BuscarCliente buscarCliente) {
        this.clientes = clientes;
        this.buscarCliente = buscarCliente;
    }

    public Cliente executar(Long id, Long versaoLida, DadosDoCliente dados) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        Cliente cliente = buscarCliente.executar(id);
        if (!Objects.equals(cliente.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Cliente", id);
        }
        cliente.alterar(dados);
        return clientes.salvar(cliente);
    }
}
