package com.example.newsnexus.controller;

import com.example.newsnexus.model.Article;
import com.example.newsnexus.model.City;
import com.example.newsnexus.repository.ArticleRepository;
import com.example.newsnexus.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;


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

        if (city == null && tag == null) {
            return articleRepository.findAll();
        }

        if (city != null && tag != null) {
            return articleRepository.findByCityAndTag(city, tag);
        }

        if (city != null) {
            if (city.equalsIgnoreCase("noCity")) {
                return articleRepository.findByCityIgnoreCaseContaining(null);
            } else {
                return articleRepository.findByCityIgnoreCaseContaining(city);
            }
        }

        if (tag != null) {
            return articleRepository.findByTagContainingIgnoreCase(tag);
        }

        return new ArrayList<>();
    }


    @GetMapping("/cities")
    public List<String> getCities() {
        return cityRepository.findAll()
                .stream()
                .map(City::getName)
                .collect(Collectors.toList());
    }
}
