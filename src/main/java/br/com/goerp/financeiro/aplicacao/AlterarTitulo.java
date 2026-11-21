package br.com.goerp.financeiro.aplicacao;

import java.time.LocalDate;
import java.util.Objects;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/** Corrige descrição, categoria, documento, valor ou vencimento, enquanto não houver pagamento. */
@CasoDeUso
public class AlterarTitulo {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;
    private final BuscarCategoriaFinanceira buscarCategoria;

    public AlterarTitulo(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa,
            BuscarCategoriaFinanceira buscarCategoria) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
        this.buscarCategoria = buscarCategoria;
    }

    public TituloConsultado executar(NaturezaDoTitulo natureza, Long id, Long versaoLida, String descricao,
            Long categoriaId, String documento, Dinheiro valor, LocalDate vencimento) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        Titulo titulo = titulosDaEmpresa.buscar(natureza, id);
        if (!Objects.equals(titulo.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Título", id);
        }
        if (categoriaId != null && !categoriaId.equals(titulo.categoriaId())) {
            buscarCategoria.executar(categoriaId).exigirQueClassifica(natureza.sentidoDaBaixa());
        }
        titulo.alterar(descricao, categoriaId, documento, valor, vencimento);
        return titulosDaEmpresa.consultado(titulos.salvar(titulo));
    }
}
