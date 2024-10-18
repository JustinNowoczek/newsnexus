package com.example.newsnexus.repository;

import com.example.newsnexus.model.Article;
import com.example.newsnexus.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByTitle(String title);

    List<Article> findByCityIgnoreCaseContaining(String city);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Article> findByTagContainingIgnoreCase(@Param("tag") String tag);

    @Query("SELECT a FROM Article a LEFT JOIN a.tags t WHERE "
            + "(LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%')) OR a.city IS NULL) "
            + "AND (t IS NULL OR LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%')))")
    List<Article> findByCityAndTag(@Param("city") String city, @Param("tag") String tag);

    @Query("SELECT n FROM Article n WHERE n.tags LIKE %:tag%")
    List<Article> findByTag(@Param("tag") String tag);

    List<Article> findByCityIsNull();

}