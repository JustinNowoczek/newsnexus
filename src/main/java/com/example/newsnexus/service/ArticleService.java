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

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Value("${textrazor.api.key}")
    private String TEXT_RAZOR_API_KEY;

    @Value("${news.api.url}")
    private String NEWS_API_URL;

    @Value("${gnews.api.url}")
    private String GNEWS_API_URL;

    @Value("${currents.api.url}")
    private String CURRENTS_API_URL;

//    public ArticleService() {
//        fetchNews();
//    }

    @Scheduled(fixedRateString = "${news.fetch.interval}")
    public void fetchNews() {
        fetchAndSaveArticles(NEWS_API_URL, "urlToImage", "publishedAt", "author", "articles");
        fetchAndSaveArticles(GNEWS_API_URL, "image", "publishedAt", "source.name", "articles");
        fetchAndSaveArticles(CURRENTS_API_URL, "image", "published", "author", "news");
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
            article.setAuthor(articleJson.optString(authorKey, null));
            article.setUrl(articleJson.optString("url", null));
            article.setImageUrl(articleJson.optString(imageUrlKey, null));
            article.setPublishDate(articleJson.optString(publishDateKey, null));

//            String cityAndTags = extractCityAndTags(article.getTitle());
//            if (cityAndTags != null) {
//                String[] parts = cityAndTags.split(";");
//                article.setCity(parts[0].equals("null") ? null : parts[0]);
//                article.setTags(parts.length > 1 ? List.of(parts[1].split(",")) : new ArrayList<>());
//            }

            articleRepository.save(article);
        }
    }

    private String extractCityAndTags(String title) {
        TextRazor client = new TextRazor(TEXT_RAZOR_API_KEY);
        client.addExtractor("entities");
        client.addExtractor("topics");

        try {
            AnalyzedText analyzedText = client.analyze(title);
            String city = null;
            List<String> tags = new ArrayList<>();

            for (Entity entity : analyzedText.getResponse().getEntities()) {
                List<String> freebaseTypes = entity.getFreebaseTypes();
                if (freebaseTypes != null && freebaseTypes.contains("/location/citytown")) {
                    city = entity.getEntityId();
                    break;
                }
            }

            for (Topic topic : analyzedText.getResponse().getTopics()) {
                tags.add(topic.getLabel());
                if (tags.size() >= 10) {
                    break;
                }
            }

            return (city != null ? city : "null") + ";" + String.join(",", tags);

        } catch (AnalysisException | NetworkException e) {
            e.printStackTrace();
            return null;
        }
    }

}
