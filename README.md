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

## UML:






Test Details:

Unit Testing:

Repository Tests:


Utility Tests:


Data Processing Tests:




File Watcher tests:




# Integration Testing:

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

   - Build Actor-System where Supervisor / Root Guardian to spawn Child-Actors to process files in directory
   - Sub-ordinate Actors Parse Lines and Create a sub-ordinate/Child Actor to process and load data to Elastic-Search-Engine
  -  By Shifting to Actor based approach, it will become a distributed System and Horizontally Scalable

# Alternative Approaches:

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

