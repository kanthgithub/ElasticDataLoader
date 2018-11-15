package com.elasticDataLoader.repository;

import com.elasticDataLoader.common.DateTimeUtil;
import com.elasticDataLoader.entity.FileData;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileDataRepositoryImpl implements  FileDataRepositoryCustom {

    Logger log = LoggerFactory.getLogger(FileDataRepositoryImpl.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * match by word Content & the timeStamp >= deltaTimeInEpochMillis
     *
     * @param word
     * @param deltaTimeInHours
     * @return Collection of FileData Entities
     */
    @Override
    public List<FileData> findByContentAndTimeLimit(String word, Long deltaTimeInHours) {

        QueryBuilder qb = getQueryBuilderForContentAndTimeRank(word, deltaTimeInHours);

        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(qb)
                .build();

        return elasticsearchTemplate.queryForList(build, FileData.class);
    }

    /**
     *
     * @param word
     * @return aggregated Count
     */
    @Override
    public Long getStatsAggregationDataByContent(String word){

        QueryBuilder qb = getQueryBuilderByWord(word);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("filedatafrequency")
                .withTypes("fileData")
                .withQuery(qb)
                .addAggregation(AggregationBuilders.count("agg").field("content")).build();

        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });

        ValueCount aggregation = aggregations.get("agg");
        Long count = aggregation.getValue();

        log.info("valueCount extracted: {}",count);

        return count;
    }

    /**
     *
     * @param word
     * @return QueryBuilder
     */
    public QueryBuilder getQueryBuilderByWord(String word) {
        return QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("content", word));
    }

    /**
     *
     * @param word
     * @param deltaTimeInHours
     * @return aggregated-count
     */
    @Override
    public Long getStatsAggregationDataByContentAndTimeRank(String word, Long deltaTimeInHours){


        QueryBuilder qb = getQueryBuilderForContentAndTimeRank(word, deltaTimeInHours);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("filedatafrequency")
                .withTypes("fileData")
                .withQuery(qb)
                .addAggregation(AggregationBuilders.count("agg").field("content")).build();

        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });

        ValueCount aggregation = aggregations.get("agg");
        Long count = aggregation.getValue();

        getMatchCountByWordAndTimeRank(word,deltaTimeInHours);

        log.info("valueCount extracted: {}",count);

        return count;
    }

    /**
     *
     * @param word
     * @param deltaTimeInHours
     * @return QueryBuilder
     */
    public QueryBuilder getQueryBuilderForContentAndTimeRank(String word, Long deltaTimeInHours) {
        Long pastTimeInEpochMillis = DateTimeUtil.convertPastTimeInHoursToEpochMillis(deltaTimeInHours.intValue());

        log.info("pastTimeInEpochMillis : {} for Hours: {}",pastTimeInEpochMillis,deltaTimeInHours);

        return QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("content", word))
                .must(QueryBuilders.rangeQuery("timestampInEpoch").gte(pastTimeInEpochMillis));
    }


    public void getMatchCountByWordAndTimeRank(String word,Long deltaTimeInHours){

        // given
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(getQueryBuilderForContentAndTimeRank(word,deltaTimeInHours))
                .withSearchType(SearchType.DEFAULT)
                .withIndices("filedatafrequency").withTypes("fileData")
                .addAggregation(AggregationBuilders.terms("matchCount").field("content")).build();

        // when
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });


        StringTerms aggregation = aggregations.get("matchCount");

        Long count = aggregation.getSumOfOtherDocCounts();

        log.info("valueCount extracted in getMatchCountByWordAndTimeRank: {}",count);
    }

}
