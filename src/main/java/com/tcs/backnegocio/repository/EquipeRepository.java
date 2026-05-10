package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Integer> {
	List<Equipe> findByEmpresaIdIn(List<Integer> empresaIds);

	List<Equipe> findDistinctByUsuariosId(Integer usuarioId);

	@Query("select u.nome from Usuario u join u.equipes e where e.id = :equipeId and coalesce(u.ativo, true) = true order by u.nome")
	List<String> findUserNamesByEquipeId(@Param("equipeId") Integer equipeId);
}