package br.com.goerp.cadastros.cliente.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.ClienteJaCadastradoException;
import br.com.goerp.cadastros.cliente.dominio.ClienteRepositorio;
import br.com.goerp.cadastros.cliente.dominio.DadosDoCliente;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;

class CasosDeUsoDeClienteTest {

    private static final Documento CPF = Documento.de("52998224725");
    private static final DadosDoCliente MARIA = new DadosDoCliente("Maria", null, null, null, null, null, null, null, null);

    ClienteRepositorio clientes = mock(ClienteRepositorio.class);
    AlterarCliente alterar = new AlterarCliente(clientes, new BuscarCliente(clientes));

    @Test
    void cadastraNaEmpresaAtual() {
        when(clientes.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente cliente = new CadastrarCliente(clientes, () -> 7L).executar(CPF, MARIA);

        assertThat(cliente.empresaId()).isEqualTo(7L);
    }

    @Test
    void naoCadastraDocumentoRepetido() {
        when(clientes.existeComDocumento(CPF)).thenReturn(true);

        assertThatThrownBy(() -> new CadastrarCliente(clientes, () -> 7L).executar(CPF, MARIA))
                .isInstanceOf(ClienteJaCadastradoException.class);
        verify(clientes, never()).salvar(any());
    }

    @Test
    void alteracaoComVersaoDesatualizadaEhRecusada() {
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(Cliente.reconstituir(1L, 7L, CPF, MARIA, true, 3L)));

        assertThatThrownBy(() -> alterar.executar(1L, 2L, MARIA)).isInstanceOf(ConflitoDeEdicaoException.class);
        verify(clientes, never()).salvar(any());
    }

    @Test
    void alteracaoExigeAVersaoLida() {
        assertThatThrownBy(() -> alterar.executar(1L, null, MARIA)).isInstanceOf(CampoObrigatorioException.class);
    }
}
