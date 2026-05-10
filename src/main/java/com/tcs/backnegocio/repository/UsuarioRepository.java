package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Optional<Usuario> findByEmail(String email);
	Optional<Usuario> findByEmailAndAtivoTrue(String email);

	@Query("select distinct u from Usuario u left join fetch u.equipes where coalesce(u.ativo, true) = true")
	java.util.List<Usuario> findAllWithEquipes();

	@EntityGraph(attributePaths = "equipes")
	Optional<Usuario> findWithEquipesByEmail(String email);

	@EntityGraph(attributePaths = "equipes")
	Optional<Usuario> findWithEquipesByEmailAndAtivoTrue(String email);

	@EntityGraph(attributePaths = "equipes")
	Optional<Usuario> findWithEquipesById(Integer id);

	@EntityGraph(attributePaths = "equipes")
	Optional<Usuario> findWithEquipesByIdAndAtivoTrue(Integer id);
}