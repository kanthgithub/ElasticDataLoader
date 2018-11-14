package com.elasticDataLoader.service;

import com.elasticDataLoader.entity.FileData;

import java.util.List;

public interface FileDataQueryService {

    public List<FileData> getAllFileDataEntities();

    public List<FileData> getAllFileDataEntitiesByWord(String word);




}
