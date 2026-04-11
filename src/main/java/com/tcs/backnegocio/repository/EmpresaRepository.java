package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
	List<Empresa> findAllByIdAdm(Integer idAdm);
}