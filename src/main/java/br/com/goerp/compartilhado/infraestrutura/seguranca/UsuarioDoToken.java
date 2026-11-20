package br.com.goerp.compartilhado.infraestrutura.seguranca;

import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;

@Component
public class UsuarioDoToken implements UsuarioAtual {

    @Override
    public Long id() {
        return UsuarioAutenticado.idUsuario()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuário autenticado"));
    }
}
