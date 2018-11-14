package com.elasticDataLoader.service;

import com.elasticDataLoader.common.FileReaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Service
public class FileWatcherService implements InitializingBean{

    Logger log = LoggerFactory.getLogger(FileWatcherService.class);

    private WatchService fileWatchService;


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

    @PreDestroy
    public void destroy(){

        if(fileWatchService !=null) {

            try {
                fileWatchService.close();
            } catch (IOException e) {
               log.error("error while closing FileWatchService",e);
            }
        }
    }


    /**
     *
     */
    @Async
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

                            List<String> lines = FileReaderUtil.readFileTextToLines(child);

                            log.info("lines read are: {} ",lines);

                            fileDataProcessingService.processFileData(lines);

                            log.info("file content as lines: {}",lines);

                        } catch (IOException x) {
                            log.error("Error while reading File Contents: {}",filename,x);
                            continue;
                        }catch (Exception x) {
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


    @Override
    public void afterPropertiesSet()  {

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
}
