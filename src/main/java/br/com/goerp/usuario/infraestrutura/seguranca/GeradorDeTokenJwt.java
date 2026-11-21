package br.com.goerp.usuario.infraestrutura.seguranca;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.infraestrutura.seguranca.JwtConfig;
import br.com.goerp.compartilhado.infraestrutura.seguranca.PropriedadesDeSeguranca;
import br.com.goerp.compartilhado.infraestrutura.seguranca.UsuarioAutenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.DadosDoTokenDeAcesso;
import br.com.goerp.usuario.aplicacao.autenticacao.GeradorDeTokenDeAcesso;
import br.com.goerp.usuario.aplicacao.autenticacao.TokenDeAcesso;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeradorDeTokenJwt implements GeradorDeTokenDeAcesso {

    private static final String EMISSOR = "goerp";

    private final JwtEncoder codificador;
    private final PropriedadesDeSeguranca propriedades;

    @Override
    public TokenDeAcesso gerar(DadosDoTokenDeAcesso dados, Instant emitidoEm) {
        Instant expiraEm = emitidoEm.plus(propriedades.validadeDoAcesso());
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(dados.usuarioId().toString())
                .issuedAt(emitidoEm)
                .expiresAt(expiraEm)
                .claim(UsuarioAutenticado.CLAIM_NOME, dados.nome())
                .claim(UsuarioAutenticado.CLAIM_PERMISSOES, dados.permissoes());
        if (dados.empresaId() != null) {
            claims.claim(UsuarioAutenticado.CLAIM_EMPRESA, dados.empresaId());
        }
        var cabecalho = JwsHeader.with(JwtConfig.ALGORITMO).build();
        String token = codificador.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
        return new TokenDeAcesso(token, expiraEm);
    }
}
