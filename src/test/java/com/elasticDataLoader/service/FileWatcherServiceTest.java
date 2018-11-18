package com.elasticDataLoader.service;

import com.elasticDataLoader.repository.FileDataRepositoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)
public class FileWatcherServiceTest {

    Logger log = LoggerFactory.getLogger(FileWatcherServiceTest.class);

    @Mock
    ElasticsearchTemplate elasticsearchTemplate;

    @Mock
    FileDataRepositoryImpl fileDataRepository;

    @InjectMocks
    FileWatcherService fileWatcherService;

    ExecutorService fixedThreadPool;


    @Before
    public void setup() throws Exception{

        ReflectionTestUtils.setField(fileDataRepository,"elasticsearchTemplate",elasticsearchTemplate);

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

        ReflectionTestUtils.setField(fileWatcherService,"fixedThreadPool",fixedThreadPool);

        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown(){
        fixedThreadPool.shutdownNow();
    }

    @Test
    public void test_assert_Process_FileData(){

        //given





    }




}
