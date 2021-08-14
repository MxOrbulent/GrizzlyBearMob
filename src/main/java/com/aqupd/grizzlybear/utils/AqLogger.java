package com.aqupd.grizzlybear.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class AqLogger {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void logError(String error) {





            /*LOGGER.error("[GrizzlyBearMod] " + error);
            LOGGER.info("$([char]0x1b)[30;31m [GrizzlyBearModEscaper] "+error+" $([char]0x1b)[0m");
            LOGGER.info("\u001B[0;31mAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        LOGGER.info("\u001B[1;37m");
        LOGGER.info("\u001B[0;31maaaaaaaaaaaaaaaaaaaa");
        LOGGER.info("\u001B[1;37m");
        LOGGER.info("\u001B[0;31m AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        LOGGER.info("\u001B[1;37m");
        LOGGER.info("\u001B[0;31m aaaaaaaaaaaaaaaaaaaa");
        LOGGER.info("\u001B[1;37m");*/
        String monikerAndErrorMessage = "[GrizzlyBearMod] "+error;
        //
        LOGGER.info(monikerAndErrorMessage);


        //System.out.println("\u0024\u0028\u005b\u0063\u0068\u0061\u0072\u005d\u0030\u0078\u0031\u0062\u0029\u005b\u0033\u0030\u003b\u0033\u0031\u006d [GrizzlyBearModEscaper22] "+error+" \u0024\u0028\u005b\u0063\u0068\u0061\u0072\u005d\u0030\u0078\u0031\u0062\u0029\u005b\u0030\u006d");



    }

    public static void logInfo(String info) {
        LOGGER.info("[GrizzlyBearMod] " + info);
    }

}
