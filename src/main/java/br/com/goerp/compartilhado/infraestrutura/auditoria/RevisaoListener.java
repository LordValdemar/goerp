package br.com.goerp.compartilhado.infraestrutura.auditoria;

import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.goerp.compartilhado.infraestrutura.seguranca.UsuarioAutenticado;

/** Preenche cada {@link Revisao} com usuário, empresa e IP da requisição atual. */
public class RevisaoListener implements RevisionListener {

    @Override
    public void newRevision(Object entidadeDeRevisao) {
        Revisao revisao = (Revisao) entidadeDeRevisao;
        UsuarioAutenticado.idUsuario().ifPresent(revisao::setUsuarioId);
        UsuarioAutenticado.idEmpresa().ifPresent(revisao::setEmpresaId);
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes requisicao) {
            revisao.setIp(requisicao.getRequest().getRemoteAddr());
        }
    }
}
