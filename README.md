# ElasticDataLoader

# Use Case:

Load Log File content to Elastic-Search Engine

# Purpose:

Full-Text query to be performed to analyse text Data and generate analytics based on text match and timestamps

# Tech-Stack:

- JDK-8 for core programming

- Spring-Boot for Service/Repository management and interaction with Elastic-Engine

- Spring-elastic-data manages all dependencies with Elastic-Search Component

- Mockito and Junit for Unit-Testing

- Embedded-Elastic-Search for Integration Testing

# How-To-Run:

  1. check JAVA_HOME, set it to JDK-8
  2. check MAVEN_HOME or M2_HOME , set it to maven-3
  3. check the file directory, please set up directory path for tests:
     ```
     /tmp/
     ** assign write access for application (remove restrictions if any)
     ```

  4. application will lookup for files in pre-configured directory
     ```
     - configured under property in application.yaml (src/main/resources)
        file:
          data:
            directory: /Users/lakshmikanth/Desktop/pocket/logdata/

     - change this to the directory you would prefer
     ```
   5. Make sure that elastic-search instance is up & running on port 9300
      - For further details on how to install and run, refer the git project:
          ```
           Git Project page: https://github.com/kanthgithub/ElasticDataSearchEngine
          ```
   6. navigate to the project directory (if you are running from command console)
   7. run command
      ```
       mvn clean install
       ```
   8. After successful build , start application :
      ```
      mvn spring-boot:run
      ```
   9. application will startup on random port, in case if you want to set to specific port:
      ```
      - open application.yaml file
      - update property:
        - server:
             port: 0
      ```


# Functional Flow:

- The log file for the ElasticDataLoader​​ is generated with each row of data in the following format:

  - {timestamp_in_epoch_millis}, {string}

- Each row records the ​timestamp_in_epoch_millis​ whereby a particular ​string​ was generated in another system.

- The ​same string could appeared more than 1 time​​ within the same log file and across the log files if it was being generated multiple times by the other system.

- Upon the zero-th minute of every hour:

  - A new log file that contains the data i​n the last hour​​ will be generated and be placed into the same folder
  - The file name format is string-generation-{yyyymmddhh}.log​.
     -  E.g. the file with name ​string-generation-2018093016.log is a file generated at 1600 hour on 30 Sep, 2018.

# Technical Flow:

1. Entry-Point for application: ElasticDataLoaderApplication.java

2. Directory watch/poll is managed by: FileWatcherService.java

3. FileWatcherService

    - Monitors file events in the pre-configured directory

    - Configuration is maintained under application.yaml (src/main/resources)

    - FileWatcherService methods:

        1. Initialisation: init() -> java's WatcherService initialized and configured , started

        ```java
            @PostConstruct
            public void init(){
                try {
                    watchService
                        = FileSystems.getDefault().newWatchService();

                    log.info("initialized FileWatcherService");
                } catch (IOException e) {
                    log.error("error while initializing FileWatchService",e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void afterPropertiesSet()  {
                fileDataRepository.deleteAll();
                processAllFilesInDirectory();
                fixedThreadPool.submit(new Runnable() {
                    @Override
                    public void run()  {
                        watchForLogFiles();
                    }
                });
            }
        ```

        2. Initial-Startup-Load data from log files recorded in 24 Hours duration: processAllFilesInDirectory

        ```java
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
            public Boolean processAllFilesInDirectory() {

                Boolean response = Boolean.TRUE;

                try {
                    Path directoryPath = Paths.get(fileDataDirectory);

                    log.info("directoryPath for pre-processing: {}", directoryPath);

                    List<Path> files = Files.walk(directoryPath)
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
                    response = Boolean.FALSE;
                }

                return  response;
            }
        ```


        3. poll for new File events: watchForLogFiles

        ```java
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
        ```

        4. validate and extract data from watchEvents: processWatchEvents

        ```java
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
        ```



        5. process parsed file Data: processFileData

        ```java
            /**
             * Process FileData
             *
             * @param child
             * @return Boolean
             */
            public Boolean processFileData(Path child) {

                String fileName = child.toFile().getName();

                Boolean isSuccessful = Boolean.TRUE;

                log.info("processing FileData : {}",fileName);

                try {
                    if (isAValidFileFormat(fileName)) {

                        log.info("fileName picked (for processing check) : {}", fileName);



                        //get timeStampEpoch for Pasttime 24 hours
                        Long pastTimeInEpochMillis = getPastTimeInEpochMillis(24);

                        //get timeStampEpoch for fileString
                        Long timeStampEpochMillisFromFileString = getTimeStampInEpochMillis(fileName);

                        log.info("pastTimeInEpochMillis: {} , timeStampEpochMillisFromFileString: {}",pastTimeInEpochMillis,timeStampEpochMillisFromFileString);

                        if (timeStampEpochMillisFromFileString>=pastTimeInEpochMillis) {

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
            ```

5. FileDataProcessingService

    - extracts String-Content and timeStamp data from file content
    - generated FileData entity with the data extract
    - persists FileData in Elastic-Search-engine via Spring-Data library

    Methods:

        1. processFileData -> bulk persistence of FileData entities
        2. getFileDataFromLine -> parse file line content FileData entity

## UML:





## Test Details:

### Unit Testing:

1. Repository Tests: src/test/java/com/elasticDataLoader/repository/
2. Utility Tests:  src/test/java/com/elasticDataLoader/common/
3. Data Processing Tests: src/test/java/com/elasticDataLoader/service/
4. File Watcher tests: src/test/java/com/elasticDataLoader/service/

### Integration Testing:

** Pending

- Reason: Embedded Elastic-Engine has issues in compatibility with Spring Dependencies

- Followup: Develop a maven plugin which starts/stops the Elastic-Engine for each integration test

- How To: Integration testing using java test-container as mentioned in:

   - https://gitlab.com/kanthgitlab/elasticsearch-integration-testing

   - Source: https://www.testcontainers.org/usage/elasticsearch_container.html



# Improvements Required:


1. Cucumber Tests for Scenarios identified in Usecases (Blocked by Embedded Elastic Engine in-compatibility with Spring)
2. Support for Elastic-Cluster (Multiple elastic nodes across Data-Centers)
3. Current system is limited by single node processing
   Current Systems does parallel processing but it is limited to number of cores/processors in the Machine
4. Distributed processing using AKKA - Actor based Programming:
=======

3. Current system is limited by single node processing
   Current Systems does parallel processing but it is limited to number of cores/processors in the Machine

4. Distributed processing using AKKA - Actor based Programming:

   - Build Actor-System where Supervisor / Root Guardian to spawn Child-Actors to process files in directory
   - Sub-ordinate Actors Parse Lines and Create a sub-ordinate/Child Actor to process and load data to Elastic-Search-Engine
   -  By Shifting to Actor based approach, it will become a distributed System and Horizontally Scalable

# Alternative Approaches:

# AKKA:

   1. Build Actor-System where Supervisor / Root Guardian to spawn Child-Actors to process files in directory
   2. Sub-ordinate Actors Parse Lines and Create a sub-ordinate/Child Actor to process and load data to Elastic-Search-Engine
   3. By Shifting to Actor based approach, it will become a distributed System and Horizontally Scalable
  
# Redisson:
1. Redis Database is a key-Value based Storage
2. Redisson is a library/framework to achieve storage and processing in Redis in Distributed way
3. Redisson Library is built on JDK Concurrent utilities (java.util.concurrent)
4. Distributed Locks, Distributed

# Chronicle-IO:
1. Off-Heap Storage mechanism
2. Offers Mechanical Sympathy where process can be pinned to specific core of machine
3. Distributed Heap Offers faster read/write mechanisms

# DevOps Improvements:
 - Add Docker configuration
 - enable/configure Piplelines for Continuous Build, Delivery & Deployment
 - Use AWS - RDS (Relational Database As a Service) for scalable feature
 - Push Docker image to AWS and run from AWS

