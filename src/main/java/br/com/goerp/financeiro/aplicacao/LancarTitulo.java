package br.com.goerp.financeiro.aplicacao;

import java.util.List;

import br.com.goerp.cadastros.cliente.aplicacao.BuscarCliente;
import br.com.goerp.cadastros.fornecedor.aplicacao.BuscarFornecedor;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.financeiro.dominio.DadosDoTitulo;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Parcelamento;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/**
 * Lança uma conta a pagar ou a receber, um título por parcela, numa transação só. Também é o
 * caminho de compras e vendas para gerar seus títulos.
 */
@CasoDeUso
public class LancarTitulo {

    private final TituloRepositorio titulos;
    private final BuscarFornecedor buscarFornecedor;
    private final BuscarCliente buscarCliente;
    private final BuscarCategoriaFinanceira buscarCategoria;
    private final EmpresaAtual empresaAtual;
    private final TitulosDaEmpresa titulosDaEmpresa;

    public LancarTitulo(TituloRepositorio titulos, BuscarFornecedor buscarFornecedor, BuscarCliente buscarCliente,
            BuscarCategoriaFinanceira buscarCategoria, EmpresaAtual empresaAtual, TitulosDaEmpresa titulosDaEmpresa) {
        this.titulos = titulos;
        this.buscarFornecedor = buscarFornecedor;
        this.buscarCliente = buscarCliente;
        this.buscarCategoria = buscarCategoria;
        this.empresaAtual = empresaAtual;
        this.titulosDaEmpresa = titulosDaEmpresa;
    }

    public List<TituloConsultado> executar(DadosDoTitulo dados, Parcelamento parcelamento) {
        exigirPessoaDaEmpresa(dados);
        if (dados.categoriaId() != null) {
            buscarCategoria.executar(dados.categoriaId()).exigirQueClassifica(dados.natureza().sentidoDaBaixa());
        }
        return Titulo.parcelas(empresaAtual.id(), dados, parcelamento).stream()
                .map(titulos::salvar)
                .map(titulosDaEmpresa::consultado)
                .toList();
    }

    /** Fornecedor ou cliente de outra empresa (ou inexistente) dá 404. */
    private void exigirPessoaDaEmpresa(DadosDoTitulo dados) {
        if (dados.pessoaId() == null) {
            return;
        }
        if (dados.natureza() == NaturezaDoTitulo.A_PAGAR) {
            buscarFornecedor.executar(dados.pessoaId());
        } else {
            buscarCliente.executar(dados.pessoaId());
        }
    }
}
