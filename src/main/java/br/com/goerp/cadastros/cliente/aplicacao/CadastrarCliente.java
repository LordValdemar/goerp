package br.com.goerp.cadastros.cliente.aplicacao;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteJaCadastradoException;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.cadastros.cliente.dominio.DadosDoCliente;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.dominio.Documento;

@CasoDeUso
public class CadastrarCliente {

    private final ClienteRepositorio clientes;
    private final EmpresaAtual empresaAtual;

    public CadastrarCliente(ClienteRepositorio clientes, EmpresaAtual empresaAtual) {
        this.clientes = clientes;
        this.empresaAtual = empresaAtual;
    }

    public Cliente executar(Documento documento, DadosDoCliente dados) {
        if (clientes.existeComDocumento(documento)) {
            throw new ClienteJaCadastradoException(documento);
        }
        return clientes.salvar(Cliente.novo(empresaAtual.id(), documento, dados));
    }
}
