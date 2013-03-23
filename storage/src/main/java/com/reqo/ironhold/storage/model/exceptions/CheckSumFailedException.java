package com.reqo.ironhold.storage.model.exceptions;

import java.io.File;

/**
 * User: ilya
 * Date: 3/22/13
 * Time: 11:26 PM
 */
public class CheckSumFailedException extends Exception {
    public CheckSumFailedException(File file) {
        super("File " + file.getAbsolutePath() + " failed checksum");
    }
}
