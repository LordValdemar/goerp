package br.com.goerp.usuario.infraestrutura.inicializacao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code goerp.plataforma.administrador.*}, vindas das variáveis GOERP_ADMIN_NOME/EMAIL/SENHA. */
@ConfigurationProperties(prefix = "goerp.plataforma.administrador")
public record PropriedadesDoAdministradorDaPlataforma(String nome, String email, String senha) {

    boolean configuradas() {
        return email != null && !email.isBlank() && senha != null && !senha.isBlank();
    }

    @Override
    public String toString() {
        return "PropriedadesDoAdministradorDaPlataforma[nome=" + nome + ", email=" + email + "]";
    }
}
