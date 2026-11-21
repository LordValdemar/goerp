package br.com.goerp.usuario.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventoDeSegurancaJpaRepository extends JpaRepository<EventoDeSegurancaJpa, Long> {
}
