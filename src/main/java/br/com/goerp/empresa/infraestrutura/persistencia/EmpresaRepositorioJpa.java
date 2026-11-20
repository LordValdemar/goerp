package br.com.goerp.empresa.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import lombok.RequiredArgsConstructor;

/** Adaptador: implementa o repositório do domínio usando JPA. */
@Repository
@RequiredArgsConstructor
class EmpresaRepositorioJpa implements EmpresaRepositorio {

    private final EmpresaJpaRepository jpa;

    @Override
    public Empresa salvar(Empresa empresa) {
        EmpresaJpa registro = empresa.id() == null ? new EmpresaJpa() : buscarRegistro(empresa.id());
        copiarParaRegistro(empresa, registro);
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        return jpa.findById(id).map(EmpresaRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComCnpj(Cnpj cnpj) {
        return jpa.existsByCnpj(cnpj);
    }

    private EmpresaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
    }

    private static void copiarParaRegistro(Empresa empresa, EmpresaJpa registro) {
        registro.setCnpj(empresa.cnpj());
        registro.setRazaoSocial(empresa.razaoSocial());
        registro.setNomeFantasia(empresa.nomeFantasia());
        registro.setAtiva(empresa.ativa());
    }

    private static Empresa paraDominio(EmpresaJpa registro) {
        return Empresa.reconstituir(registro.getId(), registro.getCnpj(), registro.getRazaoSocial(),
                registro.getNomeFantasia(), registro.isAtiva());
    }
}
