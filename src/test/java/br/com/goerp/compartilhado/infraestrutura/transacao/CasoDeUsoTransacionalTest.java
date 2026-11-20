package br.com.goerp.compartilhado.infraestrutura.transacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import br.com.goerp.suporte.TesteDeIntegracao;

/** Garante a regra "ou tudo grava, ou nada grava" para qualquer {@link CasoDeUso}. */
class CasoDeUsoTransacionalTest extends TesteDeIntegracao {

    @Autowired
    CasoDeUsoQueFalhaNoMeio casoDeUso;

    @Test
    void desfazTudoQuandoOCasoDeUsoFalhaNoMeio() {
        assertThatThrownBy(casoDeUso::executar).isInstanceOf(IllegalStateException.class);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM empresa", Integer.class)).isZero();
    }

    @CasoDeUso
    static class CasoDeUsoQueFalhaNoMeio {

        private final EmpresaRepositorio empresas;

        CasoDeUsoQueFalhaNoMeio(EmpresaRepositorio empresas) {
            this.empresas = empresas;
        }

        public void executar() {
            empresas.salvar(Empresa.nova(Cnpj.de("11222333000181"), "Primeira LTDA", null));
            throw new IllegalStateException("falha depois de gravar");
        }
    }
}
