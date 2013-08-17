package com.reqo.ironhold.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: ilya
 * Date: 8/7/13
 * Time: 9:56 AM
 */
public class FileSplitter {
    private static Logger logger = Logger.getLogger(FileSplitter.class);


    private final File sourceFile;
    private final File destinationDirectory;
    public static final int DEFAULT_CHUNK_SIZE = 100 * 1048576; // 100 megabytes;

    public FileSplitter(File sourceFile, File destinationDirectory) {
        this.sourceFile = sourceFile;
        this.destinationDirectory = destinationDirectory;
    }

    public List<FileWithChecksum> split() throws Exception {
        Objects.requireNonNull(sourceFile, "Source file is required");
        Objects.requireNonNull(destinationDirectory, "Destination directory is required");

        if (!sourceFile.exists()) throw new IllegalArgumentException("Source file does not exist");
        if (!sourceFile.canRead()) throw new IllegalArgumentException("Cannot read source File");
        if (!sourceFile.isFile()) throw new IllegalArgumentException("Source file is not a file");

        if (destinationDirectory.exists()) throw new IllegalArgumentException("Destination directory already exists");
        FileUtils.forceMkdir(destinationDirectory);
        if (!destinationDirectory.exists())
            throw new IllegalArgumentException("Failed to create destination directory");

        if (!destinationDirectory.canWrite())
            throw new IllegalArgumentException("Cannot write into destination directory");
        if (!destinationDirectory.isDirectory())
            throw new IllegalArgumentException("Destination directory is not a directory");


        int chunk = 0;
        byte[] temporary = null;

        InputStream inStream = null;
        int totalBytesRead = 0;
        int chunkSize = DEFAULT_CHUNK_SIZE;

        List<FileWithChecksum> fileList = new ArrayList();

        try {
            inStream = new BufferedInputStream(new FileInputStream(sourceFile));

            StringBuilder sb = new StringBuilder();
            long fileSize = sourceFile.length();
            while (totalBytesRead < fileSize) {
                String partName = sourceFile.getName() + ".part" + chunk;
                File partFile = new File(destinationDirectory.getAbsolutePath() + File.separator + partName);
                int bytesRemaining = (int) (fileSize - totalBytesRead);
                if (bytesRemaining < chunkSize) // Remaining Data Part is Smaller Than CHUNK_SIZE
                // CHUNK_SIZE is assigned to remain volume
                {
                    chunkSize = bytesRemaining;
                }
                temporary = new byte[chunkSize]; //Temporary Byte Array
                int bytesRead = inStream.read(temporary, 0, chunkSize);

                if (bytesRead > 0) // If bytes read is not empty
                {
                    totalBytesRead += bytesRead;
                    chunk++;
                }

                FileUtils.writeByteArrayToFile(partFile, temporary);


                logger.info("Calculating checksum for part " + chunk);

                File checksumFile = MD5CheckSum.createMD5CheckSum(partFile);

                fileList.add(new FileWithChecksum(partFile, new MD5CheckSum(checksumFile)));
                sb.append(partFile.getName());
                sb.append("\t");
                sb.append(partFile.length());
                sb.append("\n");
                logger.info("Wrote part file " + chunk + ", " + bytesRemaining + " bytes remaining");
            }

            FileUtils.writeStringToFile(new File(destinationDirectory.getAbsolutePath() + File.separator + "manifest"), sb.toString());
        } finally {
            inStream.close();
        }

        return fileList;

    }

    public File join(List<FileWithChecksum> parts, File destinationDirectory) throws IOException {
        Objects.requireNonNull(parts, "List of file parts is required");
        Objects.requireNonNull(destinationDirectory, "Destination directory is required");

        if (parts.size() == 0) throw new IllegalArgumentException("Parts list needs to contain at least one entry");
        if (!destinationDirectory.exists()) throw new IllegalArgumentException("Destination directory does not exist");
        if (!destinationDirectory.canWrite())
            throw new IllegalArgumentException("Cannot write into destination directory");
        if (!destinationDirectory.isDirectory())
            throw new IllegalArgumentException("Destination directory is not a directory");

        for (FileWithChecksum part : parts) {
            if (!part.getFile().exists()) {
                throw new IllegalArgumentException("Part " + part.getFile().getAbsolutePath() + " is missing");
            }
        }

        String destinationName = parts.get(0).getFile().getName();
        destinationName = destinationName.substring(0, destinationName.lastIndexOf("."));


        File destinationFile = new File(destinationDirectory + File.separator + destinationName);
        if (destinationFile.exists())
            throw new IllegalArgumentException("Destination file " + destinationFile.getAbsolutePath() + " already exists");

        for (FileWithChecksum part : parts) {
            logger.info("Appending " + part.getFile().getAbsolutePath());
            byte[] chunk = FileUtils.readFileToByteArray(part.getFile());
            FileOutputStream fos = new FileOutputStream(destinationFile.getAbsolutePath(), true);
            fos.write(chunk);
            fos.close();

        }

        return destinationFile;

    }

}
