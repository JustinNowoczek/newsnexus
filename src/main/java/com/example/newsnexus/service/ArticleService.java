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

import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Value("${news.api.url}")
    private String newsApiUrl;

    @Value("${gnews.api.url}")
    private String gnewsApiUrl;

    @Value("${currents.api.url}")
    private String currentsApiUrl;

    @Value("${textrazor.api.key}")
    private String textrazorApiKey;

    @Autowired
    private Environment env;

    @Scheduled(fixedRateString = "${news.fetch.interval}")
    public void fetchNews() {
        fetchAndSaveArticles(gnewsApiUrl, "image", "publishedAt", "source.name", "articles");
        fetchAndSaveArticles(newsApiUrl, "urlToImage", "publishedAt", "author", "articles");
        fetchAndSaveArticles(currentsApiUrl, "image", "published", "author", "news");
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
        TextRazor client = new TextRazor(textrazorApiKey);
        client.addExtractor("entities");
        client.addExtractor("topics");

        try {
            AnalyzedText analyzedText = client.analyze(title);
            String city = null;
            List<String> tags = new ArrayList<>();

            if (analyzedText.getResponse() == null) {
                return "null;";
            }

            List<Entity> entities = analyzedText.getResponse().getEntities();
            if (entities != null) {
                for (Entity entity : entities) {
                    List<String> freebaseTypes = entity.getFreebaseTypes();
                    if (freebaseTypes != null && freebaseTypes.contains("/location/citytown")) {
                        city = entity.getEntityId();
                        break;
                    }
                }
            }

            List<Topic> topics = analyzedText.getResponse().getTopics();
            if (topics != null) {
                for (Topic topic : topics) {
                    tags.add(topic.getLabel());
                    if (tags.size() >= 10) {
                        break;
                    }
                }
            }

            return (city != null ? city : "null") + ";" + String.join(",", tags);
        } catch (AnalysisException | NetworkException e) {
            return null;
        }
    }
}
