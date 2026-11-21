package br.com.goerp.usuario.infraestrutura.persistencia;

import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.goerp.usuario.dominio.EventoDeSeguranca;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class EventoDeSegurancaRepositorioJpa implements EventoDeSegurancaRepositorio {

    private static final int TAMANHO_MAXIMO_DO_EMAIL = 150;

    private final EventoDeSegurancaJpaRepository jpa;

    @Override
    public void registrar(EventoDeSeguranca evento) {
        var registro = new EventoDeSegurancaJpa();
        registro.setTipo(evento.tipo());
        registro.setEmailInformado(limitar(evento.emailInformado()));
        registro.setUsuarioId(evento.usuarioId());
        registro.setEmpresaId(evento.empresaId());
        registro.setMomento(evento.momento());
        registro.setIp(ipDaRequisicao());
        jpa.save(registro);
    }

    /** O e-mail informado num login falho é texto livre: corta para caber na coluna. */
    private static String limitar(String email) {
        return email == null || email.length() <= TAMANHO_MAXIMO_DO_EMAIL ? email : email.substring(0, TAMANHO_MAXIMO_DO_EMAIL);
    }

    private static String ipDaRequisicao() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes requisicao) {
            return requisicao.getRequest().getRemoteAddr();
        }
        return null;
    }
}
