package com.elasticDataLoader.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class DateTimeUtil {

    public static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofHours(8);

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    /**
     *
     * string-generation-{yyyymmddhh}.log​. E.g. the file with name ​string-generation-2018093016.log
     *
     * @param fileName
     * @return LocalDateTime
     */
    public static LocalDateTime getDateTimeFromFileString(String fileName){

        log.info("extracting DateTime for file-name-String: {}",fileName);

        LocalDateTime localDateTime = null;

        String[] fileSplitString = fileName.split("-");

        log.info("split file-name-String: {}",fileSplitString);

        int length = fileSplitString.length;

        if(length >0){

            String[] timeStringSplit = fileSplitString[length-1].split("\\.");

            log.info("timeStringSplit for parsing: {}",timeStringSplit);

            String timeString = timeStringSplit!=null && timeStringSplit.length >0 ? timeStringSplit[0] : null;

            log.info("timeString for parsing: {}",timeString);


            localDateTime = !StringUtils.isEmpty(timeString) ? LocalDateTime.parse(timeString, formatter).atOffset(ZoneOffset.ofHours(0)).toLocalDateTime() : null;
        }

        return localDateTime;
    }

    /**
     * validates if the fileName is compliant
     *
     * @param fileNameString
     * @return Boolean
     */
    public static Boolean isAValidFileFormat(String fileNameString){

        return getDateTimeFromFileString(fileNameString)!=null;

    }

    /**
     *
     * @param localDateTime
     * @return int
     */
    public static int getHourUnitFromTime(LocalDateTime localDateTime){

        return localDateTime.getHour();

    }


    /**
     *
     * @return timeStamp in epochMillis
     */
    public static Long getCurrentTimeStampInEpochMillis(){

        Instant instant = Instant.now().atOffset(ZONE_OFFSET).toInstant();
        return instant.toEpochMilli();
    }

    /**
     *
     * @return timeStamp in LocalDateTime
     */
    public static LocalDateTime getCurrentTimeStamp(){

        return LocalDateTime.now().atOffset(ZONE_OFFSET).toLocalDateTime();
    }


    /**
     *
     * @param fileString
     * @return
     */
    public static Long getTimeStampInEpochMillis(String fileString){

        LocalDateTime localDateTime = getDateTimeFromFileString(fileString);

        Instant instant2 = localDateTime.toInstant(ZONE_OFFSET);
        return instant2.toEpochMilli();

    }

    /**
     *
     * @param fileString
     * @return
     */
    public static Long getTimeDifferenceInEpochMillis(String fileString){

        return getCurrentTimeStampInEpochMillis() - getTimeStampInEpochMillis(fileString);

    }

    /**
     *
     * @param deltaHours
     * @return Long (time In Epoch Seconds of pastHour)
     */
    public static Long convertPastTimeInHoursToEpochMillis(int deltaHours){

        LocalDateTime pastTime = LocalDateTime.now().plusHours(-1 * deltaHours);
        Instant instant2 = pastTime.toInstant(ZONE_OFFSET);
        return instant2.toEpochMilli();
    }


    /**
     *
     * @param deltaHours
     * @return Long (time In Epoch Seconds of futureHour)
     */
    public static Long getFutureTimeInEpochMillis(int deltaHours){

        LocalDateTime pastTime = LocalDateTime.now().plusHours(deltaHours);
        Instant instant2 = pastTime.toInstant(ZONE_OFFSET);
        return instant2.toEpochMilli();
    }

    /**
     *
     * @param deltaHours
     * @return Long (time In Epoch Seconds of futureHour)
     */
    public static Long getPastTimeInEpochMillis(int deltaHours){

        LocalDateTime pastTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(-deltaHours);

        Instant instant2 = pastTime.toInstant(ZONE_OFFSET).truncatedTo(ChronoUnit.HOURS);
        return instant2.toEpochMilli();
    }

    /**
     *
     * @param format
     * @param date
     * @return formatted DateString
     */
    public static String getDateAsFormattedString(String format,LocalDateTime date){

        LocalDateTime dateArgument = date==null ? getCurrentTimeStamp() : date;

        String formatArgument = format!=null ? format : "yyyyMMdd";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatArgument);

        return dtf.format(dateArgument);
    }

    /**
     * Random Long number as timeStampInEpochMillis
     *
     * @return Long
     */
    public static Long getRandomTimeStampInEpochMillisFromDate(){

        long nextLong = ThreadLocalRandom.current().nextLong(5,1000); // For 2-digit integers, 10-99 inclusive.

        return (getCurrentTimeStampInEpochMillis() - nextLong);
    }



}
