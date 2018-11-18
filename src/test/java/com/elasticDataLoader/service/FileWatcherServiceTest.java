package com.elasticDataLoader.service;

import com.elasticDataLoader.common.DateTimeUtil;
import com.elasticDataLoader.repository.FileDataRepositoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.elasticDataLoader.common.DateTimeUtil.getCurrentTimeStamp;
import static com.elasticDataLoader.common.FileReaderUtil.copyDataFromSourceToDestination;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.junit.Assert.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class FileWatcherServiceTest {

    public static final String LOG = ".log";
    public static final String STRING_GENERATION = "string-generation-";

    Logger log = LoggerFactory.getLogger(FileWatcherServiceTest.class);

    @Mock
    ElasticsearchTemplate elasticsearchTemplate;

    @Mock
    FileDataRepositoryImpl fileDataRepository;

    @Mock
    FileDataProcessingService fileDataProcessingService;

    @InjectMocks
    FileWatcherService fileWatcherService;

    ExecutorService fixedThreadPool;


    //Declare and Initialize testData File References

    private static final String fileDataDirectory = "/tmp/elastic/";

    public static String test_Data_File_1 = "/testdata/testData_Shakespeare_Part_1.txt";

    public String testFile_1 = null;

    Path testFilePath_1 = null;

    public static String test_Data_File_2 = "/testdata/testData_Shakespeare_Part_2.txt";

    public String testFile_2 = null;

    Path testFilePath_2 = null;

    public static String test_Data_File_3 = "/testdata/testData_Shakespeare_Part_3.txt";

    public String testFile_3 = null;

    Path testFilePath_3 = null;


    public String testFile_4 = null;

    Path testFilePath_4 = null;



    @Before
    public void setup() throws Exception{

        ReflectionTestUtils.setField(fileDataRepository,"elasticsearchTemplate",elasticsearchTemplate);

        fixedThreadPool = Executors.newFixedThreadPool(5);

        ReflectionTestUtils.setField(fileWatcherService,"fixedThreadPool",fixedThreadPool);

        ReflectionTestUtils.setField(fileWatcherService,"fileDataDirectory",fileDataDirectory);

        cleanupTestFiles();

        prepareTestDataFiles();

        loadTestDataToTestFilesInTestDirectory();

        MockitoAnnotations.initMocks(this);
    }


    @After
    public void tearDown(){

        cleanupTestFiles();

        fixedThreadPool.shutdownNow();
    }

    private void cleanupTestFiles() {
        if(testFilePath_1!=null) {
            testFilePath_1.toFile().deleteOnExit();
        }

        if(testFilePath_2!=null) {
            testFilePath_2.toFile().deleteOnExit();
        }

        if(testFilePath_3!=null) {
            testFilePath_3.toFile().deleteOnExit();
        }
    }

    /**
     * prepares Test Data Files for FIle-Upload
     * @throws IOException
     */
    private void prepareTestDataFiles() throws IOException {

        String testDate = DateTimeUtil.getDateAsFormattedString(null,null);

        int currentHourUnit = DateTimeUtil.getHourUnitFromTime(getCurrentTimeStamp());

        testFile_1 = fileDataDirectory+ STRING_GENERATION +testDate+(currentHourUnit-1)+ LOG;

        testFilePath_1 = createNewFile(testFile_1);

        testFile_2 = fileDataDirectory+ STRING_GENERATION +testDate+(currentHourUnit-4)+ LOG;

        testFilePath_2 = createNewFile(testFile_2);

        testFile_3 = fileDataDirectory+ STRING_GENERATION +testDate+(currentHourUnit-10)+ LOG;

        testFilePath_3 = createNewFile(testFile_3);
    }

    private Path createNewFile(String fileNamePath) throws IOException {

        Path testFilePath = Paths.get(fileNamePath);

        File file = new File(fileNamePath);

        file.deleteOnExit();

        testFilePath.getParent().toFile().mkdirs();

        try {
            file.createNewFile();
        } catch (FileAlreadyExistsException e) {
            System.err.println("already exists: " + e.getMessage());
        }

        return testFilePath;
    }


    /**
     * Loads Test Data from dataInput to testFiles
     *
     * @throws Exception
     */
    public void loadTestDataToTestFilesInTestDirectory() throws  Exception{

        //given

        Path source_path_File_1 = Paths.get(getClass().getResource(test_Data_File_1).toURI());

        copyDataFromSourceToDestination(source_path_File_1,testFilePath_1);

        Path source_path_File_2 = Paths.get(getClass().getResource(test_Data_File_2).toURI());

        copyDataFromSourceToDestination(source_path_File_2,testFilePath_2);

        Path source_path_File_3 = Paths.get(getClass().getResource(test_Data_File_3).toURI());

        copyDataFromSourceToDestination(source_path_File_3,testFilePath_3);
    }

    @Test
    public void test_assert_Process_FileData() throws Exception{

        //given
        Path filePathForProcessing = testFilePath_1;

        //when
       Boolean response = fileWatcherService.processFileData(filePathForProcessing);

       //then
        assertNotNull(response);
        assertTrue(response);
        Mockito.verify(fileDataProcessingService,times(1)).processFileData(Files.readAllLines(filePathForProcessing));
    }

    @Test
    public void test_assert_For_Failure_While_Processing_FileData() throws Exception{

        //given
        Path filePathForProcessing = Paths.get("fakepath");

        //when
        Boolean response = fileWatcherService.processFileData(filePathForProcessing);

        //then
        assertNotNull(response);
        assertFalse(response);
    }


    @Test
    public void test_assert_For_filtering_Old_FileData_While_Processing_FileData() throws Exception{

        //given
        Path filePathForProcessing = createNewFile("/tmp/fakepath-2018102301.log");

        //when
        Boolean response = fileWatcherService.processFileData(filePathForProcessing);

        //then
        assertNotNull(response);
        assertTrue(response);
        Mockito.verify(fileDataProcessingService,times(0)).processFileData(Files.readAllLines(filePathForProcessing));

        filePathForProcessing.toFile().deleteOnExit();
    }


    @Test
    public void test_Assert_Process_All_Files_In_Directory() throws Exception{

        //given TestData files are placed in pre-configured directory

        //when
        Boolean response = fileWatcherService.processAllFilesInDirectory();

        //then
        assertNotNull(response);
        assertTrue(response);
        Mockito.verify(fileDataProcessingService,times(1)).processFileData(Files.readAllLines(testFilePath_1));
        Mockito.verify(fileDataProcessingService,times(1)).processFileData(Files.readAllLines(testFilePath_2));
        Mockito.verify(fileDataProcessingService,times(1)).processFileData(Files.readAllLines(testFilePath_3));
    }



    @Test
    public void assert_For_Watch_Events(){


        //given
        WatchEvent<Path> watchEvent  = new WatchEvent() {
            @Override
            public Kind kind() {
                return ENTRY_CREATE;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return testFilePath_1;
            }
        };

        //when
        Boolean response = fileWatcherService.processWatchEvents(Paths.get(fileDataDirectory),watchEvent);

        //then
        assertNotNull(response);
        assertTrue(response);
    }

    @Test
    public void assert_Watch_For_Log_Files_With_Poison_Pill() throws  Exception{

        //when
      fixedThreadPool.submit(new Runnable() {
          @Override
          public void run() {
              fileWatcherService.watchForLogFiles();
          };
      });

        Thread.sleep(1000);

        String testDate = DateTimeUtil.getDateAsFormattedString(null,null);

        int currentHourUnit = DateTimeUtil.getHourUnitFromTime(getCurrentTimeStamp());

        testFile_4 = fileDataDirectory+ STRING_GENERATION +testDate+(currentHourUnit-1)+ LOG;

        testFilePath_4 = createNewFile(testFile_4);


        Thread.sleep(3000);

        //then
        //fileWatcherService.POSION_PILL.set(Boolean.TRUE);

        testFilePath_4.toFile().deleteOnExit();
    }

}
