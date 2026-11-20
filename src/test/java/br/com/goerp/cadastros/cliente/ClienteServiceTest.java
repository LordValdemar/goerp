package br.com.goerp.cadastros.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.goerp.cadastros.cliente.dto.ClienteRequest;
import br.com.goerp.shared.exception.RecursoNaoEncontradoException;
import br.com.goerp.shared.exception.RegraDeNegocioException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    ClienteRepository repository;

    @InjectMocks
    ClienteService service;

    private final ClienteRequest pessoaFisica =
            new ClienteRequest(TipoPessoa.FISICA, "Maria Silva", "12345678901", "maria@exemplo.com", null);

    @Test
    void criaClienteQuandoDocumentoNaoExiste() {
        when(repository.existsByCpfCnpj("12345678901")).thenReturn(false);
        when(repository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        var resposta = service.criar(pessoaFisica);

        assertThat(resposta.nome()).isEqualTo("Maria Silva");
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    void rejeitaDocumentoDuplicado() {
        when(repository.existsByCpfCnpj("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(pessoaFisica))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rejeitaCnpjParaPessoaFisica() {
        var request = new ClienteRequest(TipoPessoa.FISICA, "Maria", "12345678000199", null, null);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void inativaCliente() {
        Cliente cliente = new Cliente();
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        service.inativar(1L);

        assertThat(cliente.isAtivo()).isFalse();
    }

    @Test
    void falhaAoBuscarClienteInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
