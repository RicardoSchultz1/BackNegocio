package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {

    Optional<Folder> findByIdAndDeletedFalse(Integer id);

    @Query("""
            select f from Folder f
            where f.equipe.id = :equipeId
              and f.isRoot = true
            """)
    Optional<Folder> findRootByEquipeId(@Param("equipeId") Integer equipeId);

    @Query(value = """
            select exists(
                select 1
                from folder f
                where f.nome = :nome
                  and f.equipe_id = :equipeId
                  and f.deleted = false
                  and ((:parentId is null and f.parent_id is null) or f.parent_id = :parentId)
            )
            """, nativeQuery = true)
    boolean existsActiveByNameAndParentAndEquipe(@Param("nome") String nome,
                                                  @Param("parentId") Integer parentId,
                                                  @Param("equipeId") Integer equipeId);

    @Query(value = """
            with recursive folder_tree as (
                select f.id, f.nome, f.parent_id, f.is_root, 0 as depth
                from folder f
                where f.id = :folderId
                  and f.deleted = false
                union all
                select c.id, c.nome, c.parent_id, c.is_root, ft.depth + 1
                from folder c
                join folder_tree ft on c.parent_id = ft.id
                where c.deleted = false
            )
            select id as id,
                   nome as nome,
                   parent_id as parentId,
                   is_root as isRoot,
                   depth as depth
            from folder_tree
            order by depth asc, id asc
            """, nativeQuery = true)
    List<FolderFlatProjection> findActiveTree(@Param("folderId") Integer folderId);

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
            select id from subtree
            """, nativeQuery = true)
    List<Integer> findSubtreeIds(@Param("folderId") Integer folderId);

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
            update folder
            set deleted = :deleted
            where id in (select id from subtree)
            """, nativeQuery = true)
    int markSubtreeDeleted(@Param("folderId") Integer folderId, @Param("deleted") boolean deleted);

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
            select exists(select 1 from subtree where id = :candidateId)
            """, nativeQuery = true)
    boolean isInSubtree(@Param("folderId") Integer folderId, @Param("candidateId") Integer candidateId);
}
