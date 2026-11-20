package br.com.goerp.usuario.web;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;

/** Formatos de resposta da autenticação. */
final class AutenticacaoResponses {

    private static final String TIPO_DO_TOKEN = "Bearer";

    private AutenticacaoResponses() {
    }

    record EmpresaResumo(Long id, String razaoSocial, String nomeFantasia) {

        static EmpresaResumo de(Empresa empresa) {
            return empresa == null ? null : new EmpresaResumo(empresa.id(), empresa.razaoSocial(), empresa.nomeFantasia());
        }
    }

    record UsuarioResumo(Long id, String nome, String email) {
    }

    record Tokens(
            String tokenDeAcesso,
            String tipo,
            Instant expiraEm,
            String tokenDeRenovacao,
            Instant renovacaoExpiraEm,
            UsuarioResumo usuario,
            EmpresaResumo empresa) {

        static Tokens de(Autenticado autenticado) {
            var tokens = autenticado.tokens();
            var usuario = autenticado.usuario();
            return new Tokens(tokens.acesso().valor(), TIPO_DO_TOKEN, tokens.acesso().expiraEm(),
                    tokens.tokenDeRenovacao(), tokens.renovacaoExpiraEm(),
                    new UsuarioResumo(usuario.id(), usuario.nome(), usuario.email().valor()),
                    EmpresaResumo.de(autenticado.empresa()));
        }
    }

    record SelecaoDeEmpresa(boolean requerSelecaoDeEmpresa, List<EmpresaResumo> empresas) {

        static SelecaoDeEmpresa de(List<Empresa> empresas) {
            return new SelecaoDeEmpresa(true, empresas.stream().map(EmpresaResumo::de).toList());
        }
    }

    record UsuarioAtual(Long usuarioId, String nome, Long empresaId, Collection<String> permissoes) {
    }
}
