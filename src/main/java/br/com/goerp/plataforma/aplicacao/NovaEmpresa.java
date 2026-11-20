package br.com.goerp.plataforma.aplicacao;

import br.com.goerp.empresa.aplicacao.DadosDaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.DadosDoAdministrador;

public record NovaEmpresa(DadosDaEmpresa empresa, DadosDoAdministrador administrador) {
}
