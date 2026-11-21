package br.com.goerp.cadastros.cliente.aplicacao;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class ReativarCliente {

    private final ClienteRepositorio clientes;
    private final BuscarCliente buscarCliente;

    public ReativarCliente(ClienteRepositorio clientes, BuscarCliente buscarCliente) {
        this.clientes = clientes;
        this.buscarCliente = buscarCliente;
    }

    public Cliente executar(Long id) {
        Cliente cliente = buscarCliente.executar(id);
        cliente.ativar();
        return clientes.salvar(cliente);
    }
}
