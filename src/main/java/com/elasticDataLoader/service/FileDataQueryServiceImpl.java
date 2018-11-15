package com.elasticDataLoader.service;

import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.repository.FileDataRepository;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileDataQueryServiceImpl implements  FileDataQueryService {

    @Autowired
    private FileDataRepository fileDataRepository;


    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;



    @Override
    public List<FileData> getAllFileDataEntities() {
        return Lists.newArrayList(fileDataRepository.findAll());
    }

    @Override
    public List<FileData> getAllFileDataEntitiesByWord(String word) {

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("content", word));

        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(qb)
                .build();

        return elasticsearchTemplate.queryForList(build, FileData.class);
    }

    @Override
    public List<FileData> findByContentAndTimeLimit(String word, Long deltaTimeInHours) {
        return fileDataRepository.findByContentAndTimeLimit(word,deltaTimeInHours);
    }

    @Override
    public Long getStatsAggregationData(String word) {
        return fileDataRepository.getStatsAggregationDataByContent(word);
    }


    @Override
    public Long getStatsAggregationDataAndTimeRank(String word, Long deltaTimeInHours) {
        return fileDataRepository.getStatsAggregationDataByContentAndTimeRank(word,deltaTimeInHours);
    }
}
