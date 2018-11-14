package com.elasticDataLoader.repository;

import com.elasticDataLoader.entity.FileData;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDataRepository extends ElasticsearchRepository<FileData, Integer> , FileDataRepositoryCustom{

    /**
     *
     * @param word
     * @return Collection of FileData Entities
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"content\": \"?0\"}}]}}")
    public List<FileData> finfByContent(String word);

    /**
     *
     * @param auditTime
     * @return Collection of FileData Entities
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"timestampInEpoch\": \"?0\"}}]}}")
    public List<FileData> findByAuditTime(Long auditTime);


}
