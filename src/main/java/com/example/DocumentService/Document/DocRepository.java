package com.example.DocumentService.Document;

import com.example.DocumentService.Document.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocRepository extends JpaRepository<Document, UUID> {

    Page<Document> findByOwnerId(UUID ownerId, Pageable pageable);

    List<Document> findByOwnerId(UUID ownerId);

    // Find by title containing (case-insensitive search)
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Document> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    // Find by owner and title
    @Query("SELECT d FROM Document d WHERE d.ownerId = :ownerId AND LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Document> findByOwnerIdAndTitleContainingIgnoreCase(
            @Param("ownerId") UUID ownerId,
            @Param("title") String title,
            Pageable pageable);

    // Check if document exists and belongs to owner
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    // Find document by id and owner (for security)
    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);

    // Count documents by owner
    long countByOwnerId(UUID ownerId);

    // Custom query for JSONB content search (PostgreSQL specific)
    @Query(value = "SELECT * FROM documents WHERE content::text ILIKE %:searchTerm%", nativeQuery = true)
    Page<Document> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);
}