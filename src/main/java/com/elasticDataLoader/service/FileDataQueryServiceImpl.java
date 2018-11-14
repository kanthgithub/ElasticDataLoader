package com.elasticDataLoader.service;

import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.repository.FileDataRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileDataQueryServiceImpl implements  FileDataQueryService {

    @Autowired
    private FileDataRepository fileDataRepository;


    @Override
    public List<FileData> getAllFileDataEntities() {
        return Lists.newArrayList(fileDataRepository.findAll());
    }

    @Override
    public List<FileData> getAllFileDataEntitiesByWord(String word) {
        return fileDataRepository.finfByContent(word);
    }
}
