package br.com.goerp.compartilhado.infraestrutura.transacao;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/**
 * Liga os casos de uso ao Spring sem que eles conheçam o Spring:
 * registra toda classe {@link CasoDeUso} como bean e envolve suas chamadas em uma transação.
 */
@Configuration
@ComponentScan(
        basePackages = "br.com.goerp",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CasoDeUso.class))
public class CasoDeUsoConfig {

    @Bean
    Advisor transacaoDosCasosDeUso(TransactionManager gerenciadorDeTransacao) {
        var interceptor = new TransactionInterceptor(gerenciadorDeTransacao, new MatchAlwaysTransactionAttributeSource());
        return new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(CasoDeUso.class, true), interceptor);
    }
}
