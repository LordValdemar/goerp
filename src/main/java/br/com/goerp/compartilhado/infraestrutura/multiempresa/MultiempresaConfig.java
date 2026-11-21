package br.com.goerp.compartilhado.infraestrutura.multiempresa;

import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;

@Configuration
public class MultiempresaConfig {

    @Bean
    HibernatePropertiesCustomizer resolvedorDeEmpresaNoHibernate() {
        return propriedades -> propriedades.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                new ResolvedorDeEmpresa());
    }

    /**
     * Abre o escopo "todas as empresas" ANTES da transação começar (ordem mais alta),
     * porque o Hibernate decide o filtro de empresa quando a sessão é aberta.
     */
    @Bean
    Advisor acessoEntreEmpresas() {
        MethodInterceptor interceptor = chamada -> {
            try {
                return ContextoDeEmpresa.emTodasAsEmpresas(() -> prosseguir(chamada));
            } catch (ExcecaoEmbrulhada embrulhada) {
                throw embrulhada.getCause();
            }
        };
        var advisor = new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(AcessoEntreEmpresas.class, true),
                interceptor);
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return advisor;
    }

    private static Object prosseguir(org.aopalliance.intercept.MethodInvocation chamada) {
        try {
            return chamada.proceed();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new ExcecaoEmbrulhada(e);
        }
    }

    /** Leva uma exceção checada através do {@code Supplier} e é desembrulhada no interceptor. */
    private static final class ExcecaoEmbrulhada extends RuntimeException {

        ExcecaoEmbrulhada(Throwable causa) {
            super(causa);
        }
    }
}
