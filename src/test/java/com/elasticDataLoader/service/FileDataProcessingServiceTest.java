package com.elasticDataLoader.service;

import com.elasticDataLoader.common.DateTimeUtil;
import com.elasticDataLoader.common.FileReaderUtil;
import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.repository.FileDataRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.elasticDataLoader.common.FileReaderUtil.copyDataFromSourceToDestination;
import static com.elasticDataLoader.common.StringFrequencyUtil.getStringContentFromLogString;
import static org.junit.Assert.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class FileDataProcessingServiceTest {


    public static final String LOG = ".log";
    public static final String STRING_GENERATION = "string-generation-";


    @Mock
    FileDataRepository fileDataRepository;

    @InjectMocks
    FileDataProcessingService fileDataProcessingService;

    //Declare and Initialize testData File References

    private static final String fileDataDirectory = "/tmp/elastic/processing/";

    public static String test_Data_File_1 = "/testdata/testData_Shakespeare_Part_1.txt";

    public String testFile_1 = null;

    Path testFilePath_1 = null;

    public static String test_Data_File_2 = "/testdata/testData_Shakespeare_Part_2.txt";

    public String testFile_2 = null;

    Path testFilePath_2 = null;

    public static String test_Data_File_3 = "/testdata/testData_Shakespeare_Part_3.txt";

    public String testFile_3 = null;

    Path testFilePath_3 = null;

    @Before
    public void setupTests() throws  Exception{

        cleanupTestFiles();

        prepareTestDataFiles();

        loadTestDataToTestFilesInTestDirectory();

        MockitoAnnotations.initMocks(FileDataProcessingService.class);
    }

    @After
    public void tearDown(){

        cleanupTestFiles();
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


        testFile_1 = fileDataDirectory+ STRING_GENERATION +testDate+"00"+ LOG;

        testFilePath_1 = createNewFile(testFile_1);

        testFile_2 = fileDataDirectory+ STRING_GENERATION +testDate+"04"+ LOG;

        testFilePath_2 = createNewFile(testFile_2);

        testFile_3 = fileDataDirectory+ STRING_GENERATION +testDate+"10"+ LOG;

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
    public void assert_Process_File_Lines() throws  Exception{

        //given
        List<String> fileLines = FileReaderUtil.readFileTextToLines(testFilePath_1);

        //when
        List<FileData> fileDataListEntities_Actual =
                fileDataProcessingService.processFileData(fileLines);


        //then
        assertNotNull(fileDataListEntities_Actual);
        assertTrue(!fileDataListEntities_Actual.isEmpty());
        assertThat(fileDataListEntities_Actual.size(), Matchers.equalTo(40));
    }

    @Test
    public void assert_Get_FileDataEntity_From_Line(){

        //given
        String line = "3242343242423,Line extract from testData";

        //when
        List<FileData> fileDataList_Actual =
                fileDataProcessingService.getFileDataFromLine(line);

        //then
        assertNotNull(fileDataList_Actual);
        assertTrue(!fileDataList_Actual.isEmpty());
        assertThat(fileDataList_Actual.size(), Matchers.equalTo(1));

        FileData fileData_Actual = fileDataList_Actual.get(0);

        assertNotNull(fileData_Actual);
        assertThat(fileData_Actual.getContent(), Matchers.equalTo("Line extract from testData"));
        assertThat(fileData_Actual.getTimestampInEpoch(), Matchers.equalTo(Long.valueOf("3242343242423")));
        assertNotNull(fileData_Actual.getAuditTimeInEpochMillis());
    }

    @Test
    public void assert_File_DataEntity_From_Line(){

        //given
        Long timeStampInEpochMillis = Long.valueOf("32432432432424");
        String line = "3242343242423,Line extract from testData";

        String stringContent = getStringContentFromLogString(line);


        //when
        FileData fileData_Actual =
                fileDataProcessingService.getFileData(timeStampInEpochMillis,stringContent);

        //then
        assertNotNull(fileData_Actual);
        assertThat(fileData_Actual.getContent(), Matchers.equalTo("Line extract from testData"));
        assertThat(fileData_Actual.getTimestampInEpoch(), Matchers.equalTo(timeStampInEpochMillis));
        assertNotNull(fileData_Actual.getAuditTimeInEpochMillis());
    }


    @Test
    public void assert_Process_FileData() throws  Exception{

        //given
        List<String> fileLines = FileReaderUtil.readFileTextToLines(testFilePath_1);

        //when
        List<FileData> fileDataList_Actual =  fileDataProcessingService.processFileData(fileLines);

        //then
        Mockito.verify(fileDataRepository,times(40)).save(Mockito.any(FileData.class));
    }

}
