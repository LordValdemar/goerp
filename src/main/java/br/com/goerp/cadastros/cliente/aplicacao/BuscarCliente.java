package br.com.goerp.cadastros.cliente.aplicacao;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

@CasoDeUso
public class BuscarCliente {

    private final ClienteRepositorio clientes;

    public BuscarCliente(ClienteRepositorio clientes) {
        this.clientes = clientes;
    }

    public Cliente executar(Long id) {
        return clientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
