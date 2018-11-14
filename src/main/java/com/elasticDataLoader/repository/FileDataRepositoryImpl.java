package com.elasticDataLoader.repository;

import com.elasticDataLoader.entity.FileData;

import java.util.List;

public class FileDataRepositoryImpl implements  FileDataRepositoryCustom {

    /**
     * match by word Content & the timeStamp >= deltaTimeInEpochMillis
     *
     * @param word
     * @param deltaTimeInEpochMillis
     * @return Collection of FileData Entities
     */
    @Override
    public List<FileData> findByContentAndTimeLimit(String word, Long deltaTimeInEpochMillis) {
        return null;
    }
}
