package com.example.newsnexus.service;

import com.example.newsnexus.model.Article;
import com.example.newsnexus.repository.ArticleRepository;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Topic;
import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Value("${news.api.url}")
    private String NEWS_API_URL;

    @Value("${gnews.api.url}")
    private String GNEWS_API_URL;

    @Value("${currents.api.url}")
    private String CURRENTS_API_URL;

    @Scheduled(fixedRateString = "${news.fetch.interval}")
    public void fetchNews() {
        fetchAndSaveArticles(GNEWS_API_URL +  Dotenv.load().get("GNEWS_API_KEY"), "image", "publishedAt", "source.name", "articles");
        fetchAndSaveArticles(NEWS_API_URL +  Dotenv.load().get("NEWS_API_KEY"), "urlToImage", "publishedAt", "author", "articles");
        fetchAndSaveArticles(CURRENTS_API_URL +  Dotenv.load().get("CURRENTS_API_KEY"), "image", "published", "author", "news");
    }

    private void fetchAndSaveArticles(String apiUrl, String imageUrlKey, String publishDateKey, String authorKey, String articlesKey) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray articles = jsonResponse.getJSONArray(articlesKey);

        for (int i = 0; i < articles.length(); i++) {
            JSONObject articleJson = articles.getJSONObject(i);

            Article article = new Article();
            article.setTitle(articleJson.getString("title"));

            if (articleRepository.findByTitle(article.getTitle()).isPresent()) {
                continue;
            }

            article.setAuthor(articleJson.optString(authorKey, null));
            article.setUrl(articleJson.optString("url", null));
            article.setImageUrl(articleJson.optString(imageUrlKey, null));

            String publishDateString = articleJson.optString(publishDateKey, null);
            if (publishDateString != null) {
                LocalDateTime publishDate = parsePublishDate(publishDateString);
                article.setPublishDate(publishDate.toString());
            }

            String cityAndTags = extractCityAndTags(article.getTitle());
            if (cityAndTags != null) {
                String[] parts = cityAndTags.split(";");
                article.setCity(parts[0].equals("null") ? null : parts[0]);
                article.setTags(parts.length > 1 ? List.of(parts[1].split(",")) : new ArrayList<>());
            }

            System.out.println(apiUrl);

            articleRepository.save(article);
        }
    }

    private LocalDateTime parsePublishDate(String publishDateString) {
        DateTimeFormatter formatterZ = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTimeFormatter formatterWithOffset = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

        if (publishDateString.endsWith("Z")) {
            return LocalDateTime.parse(publishDateString, formatterZ);
        } else {
            return LocalDateTime.parse(publishDateString, formatterWithOffset);
        }
    }

    private String extractCityAndTags(String title) {
        TextRazor client = new TextRazor(Dotenv.load().get("TEXTRAZOR_API_KEY"));
        client.addExtractor("entities");
        client.addExtractor("topics");

        try {
            AnalyzedText analyzedText = client.analyze(title);
            String city = null;
            List<String> tags = new ArrayList<>();

            if (analyzedText.getResponse() != null) {
                if (analyzedText.getResponse().getEntities() != null) {
                    for (Entity entity : analyzedText.getResponse().getEntities()) {
                        List<String> freebaseTypes = entity.getFreebaseTypes();
                        if (freebaseTypes != null && freebaseTypes.contains("/location/citytown")) {
                            city = entity.getEntityId();
                            break;
                        }
                    }
                }

                if (analyzedText.getResponse().getTopics() != null) {
                    for (Topic topic : analyzedText.getResponse().getTopics()) {
                        tags.add(topic.getLabel());
                        if (tags.size() >= 10) {
                            break;
                        }
                    }
                }
            }

            return (city != null ? city : "null") + ";" + String.join(",", tags);
        } catch (AnalysisException | NetworkException e) {
            return null;
        }
    }



}
