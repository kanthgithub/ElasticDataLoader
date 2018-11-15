package com.elasticDataLoader.repository;

import com.elasticDataLoader.entity.FileData;

import java.util.List;

public interface FileDataRepositoryCustom {

    /**
     *
     * match by word Content & the timeStamp >= deltaTimeInEpochMillis
     *
     * @param word
     * @param deltaTimeInHours
     * @return Collection of FileData Entities
     */
    public List<FileData> findByContentAndTimeLimit(String word, Long deltaTimeInHours);

    /**
     *
     * @param word
     * @return aggregated count
     */
    public Long getStatsAggregationDataByContent(String word);

    /**
     *
     * @param word
     * @param deltaTimeInHours
     * @return aggregated count
     */
    public Long getStatsAggregationDataByContentAndTimeRank(String word, Long deltaTimeInHours);
}
