package br.com.goerp.compartilhado.aplicacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma classe auxiliar da camada de aplicação, reutilizada por vários casos de uso.
 * É registrada como bean, mas não abre transação própria: roda na transação do caso de uso que a chamou.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServicoDeAplicacao {
}
