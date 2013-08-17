package com.reqo.ironhold.utils;

import java.io.File;

/**
 * User: ilya
 * Date: 8/12/13
 * Time: 9:08 PM
 */
public class FileWithChecksum {
    private final File file;
    private final MD5CheckSum checkSum;

    public FileWithChecksum(File file, MD5CheckSum checkSum) {
        this.file = file;
        this.checkSum = checkSum;
    }

    public File getFile() {
        return file;
    }

    public MD5CheckSum getCheckSum() {
        return checkSum;
    }
}
