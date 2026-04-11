package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Integer> {
	List<Equipe> findByEmpresaIdIn(List<Integer> empresaIds);

	List<Equipe> findDistinctByUsuariosId(Integer usuarioId);
}