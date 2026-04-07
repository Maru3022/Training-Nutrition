package com.example.trainingnutrition.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.trainingnutrition.repository.elastic")
@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfig {
    // Elasticsearch repositories are enabled only when app.elasticsearch.enabled=true
}
