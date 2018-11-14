package com.elasticDataLoader.service;

import com.elasticDataLoader.common.DateTimeUtil;
import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.repository.FileDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.elasticDataLoader.common.StringFrequencyUtil.getStringContentFromLogString;
import static com.elasticDataLoader.common.StringFrequencyUtil.getTimeStampInEpochFromLogString;

@Service
public class FileDataProcessingService {

    Logger log = LoggerFactory.getLogger(FileDataProcessingService.class);

    @Autowired
    private FileDataRepository fileDataRepository;

    /**
     *
     * @param fileLines
     * @return  List<FileData>
     */
    public List<FileData> processFileData(List<String> fileLines){

        List<FileData> fileDataList = fileLines.stream().map(content -> getFileDataFromLine(content)).collect(Collectors.toList());

        //persist FileData
        Iterable<FileData> fileDataSaved = saveAll(fileDataList);

        log.info("saved fileData entities: {}",fileDataSaved);

        return  fileDataSaved!=null ? fileDataList.stream().collect(Collectors.toList()) : null;
    }


    public List<FileData> saveAll(List<FileData> fileDataList) {

        log.info("saving FileData entities: {}",fileDataList);

        return  fileDataList.stream().map(fileData -> fileDataRepository.save(fileData)).collect(Collectors.toList());
    }


    /**
     *
     * @param line
     * @return FileData
     */
    public FileData getFileDataFromLine(String line){

        log.info("line : {} being processed for parsing: ",line);

        Long timeStampLogInEpoch = getTimeStampInEpochFromLogString(line);

        String stringContent = getStringContentFromLogString(line);

        FileData fileData = new FileData();

        fileData.setContent(stringContent);
        fileData.setTimestampInEpoch(timeStampLogInEpoch);
        fileData.setAuditTime(DateTimeUtil.getCurrentTimeStampInEpochMillis());


        log.info("fileData entity after parse: {}",fileData);

        return fileData;
    }

}
