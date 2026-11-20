package br.com.goerp.cadastros.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.goerp.cadastros.cliente.dto.ClienteRequest;
import br.com.goerp.cadastros.cliente.dto.ClienteResponse;
import br.com.goerp.shared.exception.RecursoNaoEncontradoException;
import br.com.goerp.shared.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(ClienteResponse::de);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        validarDocumento(request);
        if (repository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new RegraDeNegocioException("Já existe cliente com o CPF/CNPJ " + request.cpfCnpj());
        }
        Cliente cliente = new Cliente();
        preencher(cliente, request);
        return ClienteResponse.de(repository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        validarDocumento(request);
        Cliente cliente = buscarEntidade(id);
        if (repository.existsByCpfCnpjAndIdNot(request.cpfCnpj(), id)) {
            throw new RegraDeNegocioException("Já existe cliente com o CPF/CNPJ " + request.cpfCnpj());
        }
        preencher(cliente, request);
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void inativar(Long id) {
        buscarEntidade(id).setAtivo(false);
    }

    private Cliente buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado"));
    }

    private void validarDocumento(ClienteRequest request) {
        int esperado = request.tipoPessoa() == TipoPessoa.FISICA ? 11 : 14;
        if (request.cpfCnpj().length() != esperado) {
            throw new RegraDeNegocioException(request.tipoPessoa() == TipoPessoa.FISICA
                    ? "Pessoa física deve informar CPF com 11 dígitos"
                    : "Pessoa jurídica deve informar CNPJ com 14 dígitos");
        }
    }

    private void preencher(Cliente cliente, ClienteRequest request) {
        cliente.setTipoPessoa(request.tipoPessoa());
        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
    }
}
