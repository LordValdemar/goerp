package br.com.goerp.compartilhado.aplicacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Libera, só para este caso de uso, o acesso a dados de todas as empresas.
 *
 * <p>Por padrão, cada consulta enxerga apenas a empresa do usuário logado. Use somente quando
 * a operação acontece fora de uma empresa por natureza (ex.: login, que precisa descobrir a quais
 * empresas o usuário tem acesso; cadastro de empresas pelo administrador da plataforma).
 * Toda classe com esta anotação deve ser revisada com cuidado.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AcessoEntreEmpresas {
}
