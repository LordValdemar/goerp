package br.com.goerp.usuario.infraestrutura.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.goerp.compartilhado.infraestrutura.seguranca.PropriedadesDeSeguranca;
import br.com.goerp.usuario.aplicacao.autenticacao.PoliticaDeSessao;

@Configuration
public class AutenticacaoConfig {

    @Bean
    PoliticaDeSessao politicaDeSessao(PropriedadesDeSeguranca propriedades) {
        return new PoliticaDeSessao(propriedades.validadeDaRenovacao());
    }
}
