package br.com.goerp.compras.aplicacao;

import java.util.List;

import br.com.goerp.compras.dominio.Recebimento;
import br.com.goerp.financeiro.aplicacao.TituloConsultado;

/** O recebimento e as contas a pagar que ele gerou (nenhuma, se a nota não tem valor, ex.: bonificação). */
public record RecebimentoRegistrado(Recebimento recebimento, List<TituloConsultado> contasAPagar) {
}
