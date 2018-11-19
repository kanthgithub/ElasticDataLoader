package com.elasticDataLoader.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileReaderUtil {

    /**
     *
     * @param filePath
     * @return Collection of lines read from file
     * @throws Exception
     */
    public static List<String> readFileTextToLines(Path filePath) throws Exception{
       return Files.readAllLines(filePath);
    }

    /**
     *
     * @param sourcePath
     * @param destinationPath
     * @throws Exception
     */
    public static void copyDataFromSourceToDestination(Path sourcePath,Path destinationPath) throws  Exception{

        List<String> fileLines_1 = FileReaderUtil.readFileTextToLines(sourcePath);

        List<String> targetLines_1 = fileLines_1.stream().map(new Function<String, String>() {
            /**
             * Applies this function to the given argument.
             *
             * @param s the function argument
             * @return the function result
             */
            @Override
            public String apply(String s) {
                return DateTimeUtil.getRandomTimeStampInEpochMillisFromDate().toString()+","+s;
            }
        }).collect(Collectors.toList());

        Files.write(destinationPath,targetLines_1,Charset.defaultCharset());
    }

    /**
     * create new File at specified Location
     *
     * @param fileNamePath
     * @return Path
     * @throws IOException
     */
    public static Path createNewFile(String fileNamePath) throws IOException {

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


}
