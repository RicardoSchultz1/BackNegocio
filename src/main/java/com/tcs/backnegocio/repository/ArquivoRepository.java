package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Integer> {

    Optional<Arquivo> findByIdAndDeletedFalse(Integer id);

    List<Arquivo> findByFolderIdAndDeletedFalse(Integer folderId);

    @Query(value = """
            select exists(
                select 1
                from arquivo a
                where a.nome = :nome
                  and a.folder_id = :folderId
                  and a.deleted = false
            )
            """, nativeQuery = true)
    boolean existsActiveByNameAndFolder(@Param("nome") String nome, @Param("folderId") Integer folderId);

    @Query(value = """
            with recursive folder_tree as (
                select f.id
                from folder f
                where f.id = :folderId
                  and f.deleted = false
                union all
                select c.id
                from folder c
                join folder_tree ft on c.parent_id = ft.id
                where c.deleted = false
            )
            select a.*
            from arquivo a
            join folder_tree ft on a.folder_id = ft.id
            where a.deleted = false
            order by a.folder_id asc, a.id asc
            """, nativeQuery = true)
    List<Arquivo> findAllActiveInFolderTree(@Param("folderId") Integer folderId);

    @Modifying
    @Query(value = """
            with recursive subtree as (
                select id
                from folder
                where id = :folderId
                union all
                select f.id
                from folder f
                join subtree s on f.parent_id = s.id
            )
            update arquivo
            set deleted = :deleted
            where folder_id in (select id from subtree)
            """, nativeQuery = true)
    int markDeletedByFolderSubtree(@Param("folderId") Integer folderId, @Param("deleted") boolean deleted);
}
