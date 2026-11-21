package br.com.goerp.empresa.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.goerp.compartilhado.dominio.Cnpj;

interface EmpresaJpaRepository extends JpaRepository<EmpresaJpa, Long> {

    boolean existsByCnpj(Cnpj cnpj);

    List<EmpresaJpa> findByIdInAndAtivaTrueOrderByRazaoSocial(Collection<Long> ids);

    List<EmpresaJpa> findAllByOrderByRazaoSocial();
}
