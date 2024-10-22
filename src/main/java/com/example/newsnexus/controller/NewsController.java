package com.example.newsnexus.controller;

import com.example.newsnexus.model.Article;
import com.example.newsnexus.model.City;
import com.example.newsnexus.repository.ArticleRepository;
import com.example.newsnexus.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://newsnexus-front.onrender.com"})
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
                return articleRepository.findByCityIsNull();
            } else {
                return articleRepository.findByCityIgnoreCaseContaining(city);
            }
        }

        return articleRepository.findByTagContainingIgnoreCase(tag);
    }

    @GetMapping("/cities")
    public List<City> getCities() {
        return cityRepository.findAll();
    }
}
