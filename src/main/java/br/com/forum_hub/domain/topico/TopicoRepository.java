package br.com.forum_hub.domain.topico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface TopicoRepository extends JpaRepository<Topico, Long>, JpaSpecificationExecutor<Topico> {
}

