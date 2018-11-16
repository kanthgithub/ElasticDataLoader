package com.elasticDataLoader.configuration;

import com.elasticDataLoader.common.YamlPropertyLoaderFactory;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;

@Configuration
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableElasticsearchRepositories(basePackages = "com.elasticDataLoader.repository")
@PropertySource(value = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ComponentScan(basePackages = { "com.elasticDataLoader" })
public class ElasticDataLoaderTestConfig {




}
