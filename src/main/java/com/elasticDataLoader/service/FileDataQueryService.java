package com.elasticDataLoader.service;

import com.elasticDataLoader.entity.FileData;

import java.util.List;

public interface FileDataQueryService {

    /**
     *
     * @return
     */
    public List<FileData> getAllFileDataEntities();

    /**
     *
     * @param word
     * @return
     */
    public List<FileData> getAllFileDataEntitiesByWord(String word);

    /**
     *
     * @param word
     * @param deltaTimeInEpochMillis
     * @return
     */
    public List<FileData> findByContentAndTimeLimit(String word, Long deltaTimeInEpochMillis);

    /**
     *
     * @param word
     * @return
     */
    public Long getStatsAggregationData(String word);

    /**
     *
     * @param word
     * @param deltaTimeInHours
     * @return
     */
    Long getStatsAggregationDataAndTimeRank(String word, Long deltaTimeInHours);
}
