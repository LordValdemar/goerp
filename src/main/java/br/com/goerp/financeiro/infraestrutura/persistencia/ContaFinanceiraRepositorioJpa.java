package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ContaFinanceiraRepositorioJpa implements ContaFinanceiraRepositorio {

    private final ContaFinanceiraJpaRepository jpa;
    private final EntityManager entityManager;

    @Override
    public ContaFinanceira salvar(ContaFinanceira conta) {
        ContaFinanceiraJpa registro;
        if (conta.id() == null) {
            registro = new ContaFinanceiraJpa();
            registro.setEmpresaId(conta.empresaId());
            registro.setTipo(conta.tipo());
            registro.setSaldo(BigDecimal.ZERO);
        } else {
            // nunca grava o saldo daqui: a conta pode ter sido lida antes de um lançamento simultâneo
            registro = buscarRegistro(conta.id());
        }
        registro.setNome(conta.nome());
        registro.setPermiteSaldoNegativo(conta.permiteSaldoNegativo());
        registro.setAtiva(conta.ativa());
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<ContaFinanceira> buscarPorId(Long id) {
        return jpa.findById(id).map(ContaFinanceiraRepositorioJpa::paraDominio);
    }

    /** Relê com SELECT ... FOR UPDATE mesmo se a conta já foi lida nesta transação (senão viria o saldo antigo). */
    @Override
    public Optional<ContaFinanceira> travar(Long id) {
        return jpa.findById(id).map(registro -> {
            entityManager.refresh(registro, LockModeType.PESSIMISTIC_WRITE);
            return paraDominio(registro);
        });
    }

    @Override
    public void atualizarSaldo(ContaFinanceira conta) {
        buscarRegistro(conta.id()).setSaldo(conta.saldo().valor());
    }

    @Override
    public List<ContaFinanceira> listar(Boolean ativa) {
        var registros = ativa == null ? jpa.findAllByOrderByNome() : jpa.findByAtivaOrderByNome(ativa);
        return registros.stream().map(ContaFinanceiraRepositorioJpa::paraDominio).toList();
    }

    @Override
    public boolean existeComNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existeOutraComNome(String nome, Long idIgnorado) {
        return jpa.existsByNomeIgnoreCaseAndIdNot(nome, idIgnorado);
    }

    private ContaFinanceiraJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira", id));
    }

    private static ContaFinanceira paraDominio(ContaFinanceiraJpa registro) {
        return ContaFinanceira.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getTipo(),
                registro.getNome(), registro.isPermiteSaldoNegativo(), Dinheiro.de(registro.getSaldo()),
                registro.isAtiva(), registro.getVersao());
    }
}
