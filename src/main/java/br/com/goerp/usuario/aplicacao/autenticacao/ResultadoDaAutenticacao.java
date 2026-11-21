package br.com.goerp.usuario.aplicacao.autenticacao;

import java.util.List;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.dominio.Usuario;

/**
 * Resultado de login ou renovação. Uma recusa é um resultado, não uma exceção: assim a transação
 * é confirmada e a tentativa falha fica registrada (contador de bloqueio e evento de segurança).
 */
public sealed interface ResultadoDaAutenticacao {

    /** @param empresa nula quando o administrador da plataforma entra fora de uma empresa */
    record Autenticado(TokensEmitidos tokens, Usuario usuario, Empresa empresa) implements ResultadoDaAutenticacao {
    }

    record SelecaoDeEmpresaNecessaria(List<Empresa> empresas) implements ResultadoDaAutenticacao {
    }

    record Recusado(MotivoDaRecusa motivo) implements ResultadoDaAutenticacao {
    }
}
