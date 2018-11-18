package com.elasticDataLoader.common;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileReaderUtil {

    public static List<String> readFileTextToLines(Path filePath) throws Exception{

       return Files.readAllLines(filePath);

    }



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

}
