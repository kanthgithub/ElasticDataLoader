package com.elasticDataLoader.service;

import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.repository.FileDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elasticDataLoader.common.DateTimeUtil.getCurrentTimeStampInEpochMillis;
import static com.elasticDataLoader.common.StringFrequencyUtil.getStringContentFromLogString;
import static com.elasticDataLoader.common.StringFrequencyUtil.getTimeStampInEpochFromLogString;


/**
 *
 * Service to process File Data:
 *
 * Step-1: parse File Data as lines
 * Step-2: split lines in to words and associated log-stamp
 * Step-3: save to elastic-search engine in parallel mode
 *
 * parse, split and persistence are performed in parallel mode
 *
 */
@Service
public class FileDataProcessingService {

    Logger log = LoggerFactory.getLogger(FileDataProcessingService.class);

    @Autowired
    private FileDataRepository fileDataRepository;

    /**
     *
     * parse File Data as lines, split lines in to words and associated log-stamp
     * save to elastic-search engine in parallel mode
     *
     * parse, split and persistence are performed in parallel mode
     *
     * @param fileLines
     * @return  List<FileData>
     */
    public List<FileData> processFileData(List<String> fileLines){

        List<FileData> fileDataList = fileLines.stream().map(content -> getFileDataFromLine(content))
                                                        .flatMap(list -> list.stream())
                                                        .collect(Collectors.toList());

        //persist FileData
        Iterable<FileData> fileDataSaved = fileDataList.parallelStream()
                                                       .map(fileData -> fileDataRepository.save(fileData))
                                                       .collect(Collectors.toList());

        log.info("saved fileData entities: {}",fileDataSaved);

        return  fileDataSaved!=null ? fileDataList.stream().collect(Collectors.toList()) : null;
    }


    /**
     *
     * convert FileData (lines) to a collection of FileData entities
     *
     * @param line
     * @return FileData
     */
    public List<FileData> getFileDataFromLine(String line){

        log.info("line : {} being processed for parsing: ",line);

        Long timeStampLogInEpoch = getTimeStampInEpochFromLogString(line);

        String stringContent = getStringContentFromLogString(line);

        String[] wordArray = stringContent.split("[ ]+|\n+");

        return Arrays.stream(wordArray).parallel()
                                       .map((word) -> getFileData(timeStampLogInEpoch, word))
                                       .collect(Collectors.toList());
    }

    /**
     *
     * parse Word extract from line as FileData entity
     *
     * @param timeStampLogInEpoch
     * @param stringContent
     * @return FileData
     */
    public FileData getFileData(Long timeStampLogInEpoch, String stringContent) {
        FileData fileData = new FileData();
        fileData.setId(UUID.randomUUID().toString());
        fileData.setContent(stringContent);
        fileData.setTimestampInEpoch(timeStampLogInEpoch);
        fileData.setAuditTimeInEpochMillis(getCurrentTimeStampInEpochMillis());
        log.info("fileData entity after parse: {}",fileData);
        return fileData;
    }

}
