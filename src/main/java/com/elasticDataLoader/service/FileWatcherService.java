package com.elasticDataLoader.service;

import com.elasticDataLoader.common.DateTimeUtil;
import com.elasticDataLoader.common.FileReaderUtil;
import com.elasticDataLoader.repository.FileDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.elasticDataLoader.common.DateTimeUtil.isAValidFileFormat;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Service
public class FileWatcherService implements InitializingBean{

    Logger log = LoggerFactory.getLogger(FileWatcherService.class);

    private WatchService fileWatchService;

    @Autowired
    FileDataRepository fileDataRepository;


    @Autowired
    ExecutorService fixedThreadPool;

    @Value("${file.data.directory}")
    private String fileDataDirectory;

    @Autowired
    private FileDataProcessingService fileDataProcessingService;

    @PostConstruct
    public void init(){
        try {
            fileWatchService
                = FileSystems.getDefault().newWatchService();

            log.info("initialized FileWatcherService");

        } catch (IOException e) {
            log.error("error while initializing FileWatchService",e);
        }

    }



    @Override
    public void afterPropertiesSet()  {

        fileDataRepository.deleteAll();

        processAllFilesInDirectory();

        fixedThreadPool.submit(new Runnable() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @throws Exception if unable to compute a result
             */
            @Override
            public void run()  {
                watchForLogFiles();
            }
        });

    }

    @PreDestroy
    public void destroy(){

        if(fileWatchService !=null) {

            try {
                fileWatchService.close();
                fixedThreadPool.shutdown();
            } catch (IOException e) {
               log.error("error while closing FileWatchService",e);
            }
        }
    }


    /**
     *
     */
    public void watchForLogFiles() {

        try {
            WatchService watchService
                    = FileSystems.getDefault().newWatchService();

            Path directoryPath = Paths.get(fileDataDirectory);

            log.info("directoryPath: {}",directoryPath);

            directoryPath.register(watchService, ENTRY_CREATE);

            WatchKey key;

            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {

                    log.info(
                            "Event kind:" + event.kind()
                                    + ". File affected: " + event.context() + ".");

                    if(event.kind().equals(ENTRY_CREATE)){

                        // The filename is the
                        // context of the event.
                        WatchEvent<Path> ev = (WatchEvent<Path>)event;

                        Path filename = ev.context();

                        Path child = null;

                        // Verify that the new
                        //  file is a text file.
                        try {
                            // Resolve the filename against the directory.
                            // If the filename is "test" and the directory is "foo",
                            // the resolved name is "test/foo".
                            child = directoryPath.resolve(filename);

                            processFileData(child);

                        } catch (Exception x) {
                            log.error("Error while reading File Contents: {}",filename,x);
                            continue;
                        }

                    }


                }
                key.reset();
            }
        }catch (Exception ex){
            log.error("Error while reading File Contents",ex);
        }


    }

    private Boolean processFileData(Path child) {

        String fileName = child.toFile().getName();

        Boolean isSuccessful = Boolean.TRUE;

        log.info("processing FileData : {}",fileName);

        try {
            if (isAValidFileFormat(fileName)) {

                log.info("fileName picked (for processing check) : {}", fileName);

                int hourUnit = DateTimeUtil.getHourUnitFromString(fileName);

                if (hourUnit <= 24) {

                    log.info("fileName picked for processing: {}", fileName);

                    List<String> lines = null;

                    lines = FileReaderUtil.readFileTextToLines(child);

                    log.info("lines read are: {} ", lines);

                    fileDataProcessingService.processFileData(lines);

                    log.info("file content as lines: {}", lines);
                } else {
                    log.info("Ignored processing for file: {} - as file is older than 24 hours", fileName);
                }
            }else{
                log.warn("intercepted file with non-compliant fileName - ignoring fileProcessing for:{}",fileName);
            }

        } catch(IOException x){
            log.error("Error while reading File Contents: {}", fileName, x);
        }catch(Exception e){
            log.error("Exception caught while processing file: {} in directory: {}", fileName, fileDataDirectory);
            isSuccessful = Boolean.FALSE;
        }

        return isSuccessful;
    }


    public void processAllFilesInDirectory() {

        try {
            Path directoryPath = Paths.get(fileDataDirectory);

            log.info("directoryPath for pre-processing: {}", directoryPath);

            List<Path> files = Files.walk(directoryPath)
                    .filter(Files::isRegularFile).collect(Collectors.toList());

            log.info("identified files: {}",files);

            for(Path file : files){

                log.info("about to process pending file: {}",file);

                processFileData(file);
            }

        }catch (Exception ex){
            log.error("Exception caught while processing pending files in directory: {}",fileDataDirectory);
        }
    }



}
