package com.elasticDataLoader.web;

import com.elasticDataLoader.entity.FileData;
import com.elasticDataLoader.service.FileDataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fileData")
public class FileDataQueryController {


    @Autowired
    private FileDataQueryService fileDataQueryService;

    /**
     *  Extracts all  FileData entities recorded in elasticSearch
     *
     *  HTTPCode: 200 (Success) / 500 (Internal Server Error)
     *
     * @return all FileData entities recorded in elasticSearch
     */
    @RequestMapping(path="/all", method = RequestMethod.GET)
    public ResponseEntity<List<FileData>> getAllFileDataEntities()  {

        List<FileData> fileDataList = fileDataQueryService.getAllFileDataEntities();

        return fileDataList!=null ?  ResponseEntity.ok(fileDataList) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     *  Extracts all  FileData entities with matching content (input argument)
     *
     *  HTTPCode: 200 (Success) / 500 (Internal Server Error)
     *
     * @return all FileData entities recorded in elasticSearch - with matching content (input argument)
     */
    @RequestMapping(path="/{word}", method = RequestMethod.GET)
    public ResponseEntity<List<FileData>> getAllFileDataEntitiesFilteredByWord(@PathVariable String word)  {

        List<FileData> fileDataList = fileDataQueryService.getAllFileDataEntitiesByWord(word);

        return fileDataList!=null ?  ResponseEntity.ok(fileDataList) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     *  Extracts all  FileData entities with matching content (input argument)
     *
     *  HTTPCode: 200 (Success) / 500 (Internal Server Error)
     *
     * @return all FileData entities recorded in elasticSearch - with matching content (input argument)
     */
    @RequestMapping(path="/{word}/{timeInHours}", method = RequestMethod.GET)
    public ResponseEntity<List<FileData>> getAllRankedFileDataEntitiesFilteredByWord(@PathVariable String word,@PathVariable String timeInHours)  {

        List<FileData> fileDataList = fileDataQueryService.findByContentAndTimeLimit(word,Long.valueOf(timeInHours));

        return fileDataList!=null ?  ResponseEntity.ok(fileDataList) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     *
     * @param word
     * @return aggregated stats for matching words
     */
    @RequestMapping(path="/stats/{word}", method = RequestMethod.GET)
    public ResponseEntity<Long> getStatsByWord(@PathVariable String word)  {

        Long stats = fileDataQueryService.getStatsAggregationData(word);

        return stats!=null ?  ResponseEntity.ok(stats) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     *
     * @param word
     * @param timeInHours
     * @return aggregated stats for matching words ranked by timeDelta
     */
    @RequestMapping(path="/stats/{word}/{timeInHours}", method = RequestMethod.GET)
    public ResponseEntity<Long> getStatsByWordAndTimeRank(@PathVariable String word,@PathVariable String timeInHours)  {

        Long stats = fileDataQueryService.getStatsAggregationDataAndTimeRank(word,Long.valueOf(timeInHours));

        return stats!=null ?  ResponseEntity.ok(stats) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
