package br.com.goerp.compartilhado.infraestrutura.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.aplicacao.PermissoesAtuais;

@Component
public class PermissoesDoToken implements PermissoesAtuais {

    @Override
    public boolean possui(String permissao) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao != null && autenticacao.getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals(permissao));
    }
}
