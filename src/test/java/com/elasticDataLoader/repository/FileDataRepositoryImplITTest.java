package com.elasticDataLoader.repository;

import com.elasticDataLoader.configuration.ElasticDataLoaderTestConfig;
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {ElasticDataLoaderTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@OverrideAutoConfiguration(enabled = true)
public class FileDataRepositoryImplITTest {

    @Autowired
     FileDataRepositoryImpl fileDataRepository;

    private static ElasticsearchClusterRunner runner;


/*    @BeforeClass
    public static void setup() throws IOException {
        runner = new ElasticsearchClusterRunner();
        runner.build(ElasticsearchClusterRunner.newConfigs()
                .baseHttpPort(9200)
                .numOfNode(1)
                .disableESLogger());
        runner.ensureYellow();
    }

    @AfterClass
    public static void teardown() throws Exception {

        if(runner!=null) {
            runner.close();
            runner.clean();
        }
    }*/

    @Test
    public void assert_basic_checks_On_Elastic_Database(){

        //given
        String word = "test";

        //when
        //List<FileData> fileDataList =fileDataRepository.findByContent(word);

        //then
        //assertNull(fileDataList);
    }


}
