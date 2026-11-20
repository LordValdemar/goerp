package br.com.goerp.compartilhado.aplicacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma classe como caso de uso da aplicação.
 *
 * <p>A infraestrutura registra automaticamente essas classes como beans e executa cada
 * chamada pública dentro de uma transação: ou tudo grava, ou nada grava. Assim a camada de
 * aplicação não precisa depender do Spring.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CasoDeUso {
}
