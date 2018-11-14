package com.elasticDataLoader.configuration;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;

import java.net.InetAddress;


@Configuration
@EnableElasticsearchRepositories(basePackages = "com.elasticDataLoader.repository")
@ComponentScan(basePackages = { "com.elasticDataLoader" })
public class ElasticSearchConfig {

    @Value("${spring.data.elasticsearch.host}")
    private String esHost;

    @Value("${spring.data.elasticsearch.port}")
    private int esPort;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String esClusterName;

    @Bean
    public Client client() throws Exception {

       Settings esSettings = Settings.builder()
                .put("client.transport.sniff", true)
                .put("client.transport.ignore_cluster_name", true)
               .put("cluster.name",esClusterName)
               .put("http.enabled", true)
                .build();

        TransportClient client = new PreBuiltTransportClient(esSettings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        return client;

    }

    //@Bean
    public Client embeddedClient() throws  Exception{

        EmbeddedElastic embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("2.3.3")
                .withClusterName(esClusterName)
                .withPortNumber(esPort)
                .build()
                .start();

        return embeddedElastic.createClient();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
        return new ElasticsearchTemplate(client());
    }

}
