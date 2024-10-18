package com.example.newsnexus.controller;

import com.example.newsnexus.model.Article;
import com.example.newsnexus.model.City;
import com.example.newsnexus.repository.ArticleRepository;
import com.example.newsnexus.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/articles")
    public List<Article> getArticles(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String tag) {

        List<Article> articles = articleRepository.findAll();

        if (city != null) {
            if (city.equalsIgnoreCase("null")) {
                articles = articles.stream()
                        .filter(article -> article.getCity() == null)
                        .collect(Collectors.toList());
            } else {
                articles = articles.stream()
                        .filter(article -> article.getCity().toLowerCase().contains(city.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (tag != null) {
            articles = articles.stream()
                    .filter(article -> article.getTags() != null &&
                            article.getTags().stream().anyMatch(t -> t.toLowerCase().contains(tag.toLowerCase())))
                    .collect(Collectors.toList());
        }


        return articles;
    }


    @GetMapping("/cities")
    public List<String> getCities() {
        return cityRepository.findAll()
                .stream()
                .map(City::getName)
                .collect(Collectors.toList());
    }
}
