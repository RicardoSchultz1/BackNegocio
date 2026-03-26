package com.tcs.backnegocio.repository;

public interface FolderFlatProjection {

    Integer getId();

    String getNome();

    Integer getParentId();

    Boolean getIsRoot();

    Integer getDepth();
}
