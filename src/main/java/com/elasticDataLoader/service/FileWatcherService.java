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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.elasticDataLoader.common.DateTimeUtil.isAValidFileFormat;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Service
public class FileWatcherService implements InitializingBean{

    Logger log = LoggerFactory.getLogger(FileWatcherService.class);

    private WatchService watchService;

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
            watchService
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

        if(watchService !=null) {

            try {
                watchService.close();
                fixedThreadPool.shutdown();
            } catch (IOException e) {
               log.error("error while closing FileWatchService",e);
            }
        }
    }


    /**
     * watch/poll for Log files in configured directory
     * on a NewFile event, verify and validate the fileName and timeStamp String in fileName
     * If file is valid and has arrived in 24-hours,
     * Initiate fileProcessing
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

                List<WatchEvent<?>> watchEvents = key.pollEvents();

                watchEvents.parallelStream().forEach(new Consumer<WatchEvent<?>>() {
                    @Override
                    public void accept(WatchEvent<?> watchEvent) {
                        fixedThreadPool.submit(new Callable<Boolean>() {


                            /**
                             * Computes a result, or throws an exception if unable to do so.
                             *
                             * @return computed result
                             * @throws Exception if unable to compute a result
                             */
                            @Override
                            public Boolean call() throws Exception {
                                return processWatchEvents(directoryPath,watchEvent);
                            }
                        });
                    }
                });

                key.reset();
            }
        }catch (Exception ex){
            log.error("Error while reading File Contents",ex);
        }


    }


    /**
     *
     * process File-Watch-Event
     *
     * @param directoryPath
     * @param event
     * @return result as Boolean
     */
    public Boolean processWatchEvents(Path directoryPath,WatchEvent event){

        log.info(
                "Event kind:" + event.kind()
                        + ". File affected: " + event.context() + ".");

        Boolean result = Boolean.TRUE;

        if(event.kind().equals(ENTRY_CREATE)) {

            // The filename is the
            // context of the event.
            WatchEvent<Path> ev = (WatchEvent<Path>) event;

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
                log.error("Error while reading File Contents: {}", filename, x);
                result = Boolean.FALSE;
            }
        }

        return result;
    }


    /**
     *
     * Process FileData
     *
     * @param child
     * @return Boolean
     */
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

        } catch(IOException ioExceptionObject){
            log.error("Error while reading File Contents: {}", fileName, ioExceptionObject);
        }catch(Exception exceptionObject){
            log.error("Exception caught while processing file: {} in directory: {}", fileName, fileDataDirectory,exceptionObject);
            isSuccessful = Boolean.FALSE;
        }

        return isSuccessful;
    }

    /**
     * Process all files in a configured directory
     *
     * This is a one-time activity which is triggered on startup
     *
     * Step-1: Loop(Parallel) through all files In the directory (parallel Stream for faster processing)
     *
     * Step-2: Initiate file Processing for each File
     *
     */
    public void processAllFilesInDirectory() {

        try {
            Path directoryPath = Paths.get(fileDataDirectory);

            log.info("directoryPath for pre-processing: {}", directoryPath);

            List<Path> files = Files.walk(directoryPath).parallel()
                    .filter(Files::isRegularFile).collect(Collectors.toList());

            log.info("identified files: {}",files);

            files.forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    fixedThreadPool.submit(new Callable<Boolean>() {
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public Boolean call() throws Exception {

                            log.info("about to process pending file: {}",path);

                            return processFileData(path);
                        }
                    });
                }
            });


        }catch (Exception ex){
            log.error("Exception caught while processing pending files in directory: {}",fileDataDirectory);
        }
    }



}
