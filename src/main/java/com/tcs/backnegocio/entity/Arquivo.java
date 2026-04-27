package com.tcs.backnegocio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "arquivo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_arquivo_nome_folder", columnNames = {"nome", "folder_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "folder")
@ToString(exclude = "folder")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 180)
    private String nome;

    @Column(nullable = false, length = 400)
    private String path;

    @Column(name = "file_hash", nullable = false, length = 64, unique = true)
    private String fileHash;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column
    private Long tamanho;

    @Column(length = 120)
    private String tipo;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private DocumentStatus status;

    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    @Column(nullable = false)
    private Boolean deleted;

    @CreationTimestamp
    @Column(name = "data_upload", nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
