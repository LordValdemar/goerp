package br.com.goerp.compartilhado.infraestrutura.transacao;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.Ordered;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;

/**
 * Liga os casos de uso ao Spring sem que eles conheçam o Spring:
 * registra as classes {@link CasoDeUso} e {@link ServicoDeAplicacao} como beans e envolve as chamadas
 * de cada caso de uso em uma transação.
 */
@Configuration
@ComponentScan(
        basePackages = "br.com.goerp",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = {CasoDeUso.class, ServicoDeAplicacao.class}))
public class CasoDeUsoConfig {

    @Bean
    Advisor transacaoDosCasosDeUso(TransactionManager gerenciadorDeTransacao) {
        var interceptor = new TransactionInterceptor(gerenciadorDeTransacao, new MatchAlwaysTransactionAttributeSource());
        var advisor = new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(CasoDeUso.class, true), interceptor);
        advisor.setOrder(Ordered.LOWEST_PRECEDENCE);
        return advisor;
    }
}
