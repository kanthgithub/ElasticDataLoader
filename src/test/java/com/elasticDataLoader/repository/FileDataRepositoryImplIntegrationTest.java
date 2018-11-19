package com.elasticDataLoader.repository;

import com.elasticDataLoader.configuration.ElasticDataLoaderTestConfig;
import com.elasticDataLoader.configuration.ElasticSearchTestConfig;
import com.elasticDataLoader.entity.FileData;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertNull;

@Ignore
@RunWith( SpringRunner.class )
@ContextConfiguration(classes = {ElasticSearchTestConfig.class,ElasticDataLoaderTestConfig.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class FileDataRepositoryImplIntegrationTest {

    @Autowired
    private FileDataRepository fileDataRepository;

    @Test
    public void assert_basic_checks_On_Elastic_Database(){

        //given
        String word = "test";

        //when
        List<FileData> fileDataList = fileDataRepository.findByContent(word);

        //then
        assertNull(fileDataList);
    }


}
