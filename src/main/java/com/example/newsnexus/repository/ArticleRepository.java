package com.example.newsnexus.repository;

import com.example.newsnexus.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByCity(String city);

    List<Article> findByCityIsNull();

    Optional<Article> findByTitle(String title);

    @Query("SELECT n FROM Article n WHERE n.tags LIKE %:tag%")
    List<Article> findByTag(@Param("tag") String tag);
}