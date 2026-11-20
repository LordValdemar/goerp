package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

@CasoDeUso
public class ConsultarPosicaoDeEstoque {

    private final ConsultasDeEstoque consultas;

    public ConsultarPosicaoDeEstoque(ConsultasDeEstoque consultas) {
        this.consultas = consultas;
    }

    public Pagina<PosicaoDeEstoque> executar(FiltroDePosicoes filtro, Paginacao paginacao) {
        return consultas.posicoes(filtro, paginacao);
    }
}
