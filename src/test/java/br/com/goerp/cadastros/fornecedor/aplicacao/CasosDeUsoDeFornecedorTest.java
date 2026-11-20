package br.com.goerp.cadastros.fornecedor.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorJaCadastradoException;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;

class CasosDeUsoDeFornecedorTest {

    private static final Documento CNPJ = Documento.de("11222333000181");
    private static final DadosDoFornecedor DADOS = new DadosDoFornecedor("Distribuidora", null, null, null, null, null,
            null, null);

    FornecedorRepositorio fornecedores = mock(FornecedorRepositorio.class);

    @Test
    void cadastraNaEmpresaAtual() {
        when(fornecedores.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertThat(new CadastrarFornecedor(fornecedores, () -> 7L).executar(CNPJ, DADOS).empresaId()).isEqualTo(7L);
    }

    @Test
    void naoCadastraDocumentoRepetido() {
        when(fornecedores.existeComDocumento(CNPJ)).thenReturn(true);

        assertThatThrownBy(() -> new CadastrarFornecedor(fornecedores, () -> 7L).executar(CNPJ, DADOS))
                .isInstanceOf(FornecedorJaCadastradoException.class);
        verify(fornecedores, never()).salvar(any());
    }

    @Test
    void alteracaoComVersaoDesatualizadaEhRecusada() {
        when(fornecedores.buscarPorId(1L)).thenReturn(Optional.of(Fornecedor.reconstituir(1L, 7L, CNPJ, DADOS, true, 5L)));

        assertThatThrownBy(() -> new AlterarFornecedor(fornecedores, new BuscarFornecedor(fornecedores))
                .executar(1L, 4L, DADOS)).isInstanceOf(ConflitoDeEdicaoException.class);
    }
}
