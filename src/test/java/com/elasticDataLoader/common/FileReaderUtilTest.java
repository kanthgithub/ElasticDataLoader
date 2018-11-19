package com.elasticDataLoader.common;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.elasticDataLoader.common.FileReaderUtil.copyDataFromSourceToDestination;
import static com.elasticDataLoader.common.FileReaderUtil.createNewFile;
import static com.elasticDataLoader.common.FileReaderUtil.readFileTextToLines;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileReaderUtilTest {

    private static  String fileDataDirectory = "/tmp/elastic/util/";

    public static String test_Data_File_1 = "/testdata/testData_Shakespeare_Part_1.txt";

    public String testFile_1 = null;

    Path testFilePath_1 = null;

    @Before
    public void setup(){
        if(testFilePath_1!=null) {
            testFilePath_1.toFile().deleteOnExit();
        }
    }

    @After
    public void cleanUp(){
        if(testFilePath_1!=null) {
            testFilePath_1.toFile().deleteOnExit();
        }
    }

    @Test
    public void test_Assert_Read_FileText_To_Lines() throws Exception{

        //given
        Path source_path_File_1 = Paths.get(getClass().getResource(test_Data_File_1).toURI());

        //when
        List<String> linesReadFromFile = readFileTextToLines(source_path_File_1);

        //then
        assertNotNull(linesReadFromFile);
        assertTrue(linesReadFromFile.size()==40);
    }

    @Test
    public void test_Assert_Copy_File_Contents_From_Source_To_Destination() throws Exception{

        //given
        Path source_path_File_1 = Paths.get(getClass().getResource(test_Data_File_1).toURI());

        testFile_1 = fileDataDirectory+ "targetData.log";

        testFilePath_1 = createNewFile(testFile_1);

        //when
        copyDataFromSourceToDestination(source_path_File_1,testFilePath_1);

        //then
        List<String> destinationLines = readFileTextToLines(testFilePath_1);

        assertNotNull(destinationLines);
        assertTrue(destinationLines.size()==40);
    }

    @Test
    public void test_Assert_Create_New_File() throws Exception{

        //given
        testFile_1 = fileDataDirectory+ "targetData.log";

        //when
        testFilePath_1 = createNewFile(testFile_1);

        //then
        assertNotNull(testFilePath_1);
        assertTrue(testFilePath_1.toFile().exists());
    }

}
