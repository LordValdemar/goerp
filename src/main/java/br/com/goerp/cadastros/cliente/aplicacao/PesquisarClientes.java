package br.com.goerp.cadastros.cliente.aplicacao;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.cadastros.cliente.dominio.FiltroDeClientes;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

@CasoDeUso
public class PesquisarClientes {

    private final ClienteRepositorio clientes;

    public PesquisarClientes(ClienteRepositorio clientes) {
        this.clientes = clientes;
    }

    public Pagina<Cliente> executar(FiltroDeClientes filtro, Paginacao paginacao) {
        return clientes.pesquisar(filtro, paginacao);
    }
}
