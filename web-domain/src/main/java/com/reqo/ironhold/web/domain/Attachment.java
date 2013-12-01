package com.reqo.ironhold.web.domain;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.concurrent.*;

public class Attachment {
    private static Logger logger = Logger.getLogger(Attachment.class);

    private int size;
    private Date creationTime;
    private Date modificationTime;
    private String fileName;
    private String body;
    private String contentType;
    private String contentDisposition;
    private String fileExt;

    public Attachment() {

    }

    public Attachment(int size, Date creationTime, Date modificationTime,
                      String fileName, String body, String contentType, String contentDisposition) {
        super();
        this.size = size;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.fileName = fileName;
        this.fileExt = FilenameUtils.getExtension(fileName);
        this.body = body;
        this.contentType = contentType;
        this.contentDisposition = contentDisposition;
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.fileExt = FilenameUtils.getExtension(fileName);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }


    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        if (fileExt != null && !fileExt.equals(StringUtils.EMPTY)) {
            this.fileExt = fileExt;
        } else if (fileName != null && !fileName.equals(StringUtils.EMPTY)) {
            this.fileExt = FilenameUtils.getExtension(fileName);
        }
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);

    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static IndexedAttachment toIndexedAttachment(Attachment sourceAttachment, boolean extractTextFromAttachments) {

        IndexedAttachment indexedAttachment = new IndexedAttachment();
        indexedAttachment.setContentType(sourceAttachment.getContentType());
        indexedAttachment.setContentDisposition(sourceAttachment.getContentDisposition());
        indexedAttachment.setCreationTime(sourceAttachment.getCreationTime());
        indexedAttachment.setFileExt(sourceAttachment.getFileExt());
        indexedAttachment.setFileName(sourceAttachment.getFileName());
        indexedAttachment.setModificationTime(sourceAttachment.getModificationTime());
        indexedAttachment.setSize(sourceAttachment.getSize());


        return indexedAttachment;
    }

}
