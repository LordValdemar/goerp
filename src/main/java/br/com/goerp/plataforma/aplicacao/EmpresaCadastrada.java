package br.com.goerp.plataforma.aplicacao;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.gestao.AcessoInicial;

public record EmpresaCadastrada(Empresa empresa, AcessoInicial acessoInicial) {
}
