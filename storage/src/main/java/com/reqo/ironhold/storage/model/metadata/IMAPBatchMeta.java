package com.reqo.ironhold.storage.model.metadata;

import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class IMAPBatchMeta {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }


    private IMAPMessageSource source;
    private Date started;
    private Date finished;
    private long messages;
    private long duplicates;
    private long failures;

    private long batchSize;
    private long maxSize;
    private long compressedMaxSize;

    private long messagesWithAttachments;
    private long messagesWithoutAttachments;

    @JsonIgnore
    private Mean sizeMean = new Mean();
    @JsonIgnore
    private Median sizeMedian = new Median();
    @JsonIgnore
    private Mean compressedSizeMean = new Mean();
    @JsonIgnore
    private Median compressedSizeMedian = new Median();
    private double compressedAverageSize;
    private double averageSize;
    private double medianSize;
    private double medianCompressedSize;
    @JsonIgnore
    private boolean isDirty = false;

    public String serialize() {
        try {
            persistCalculations();
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IMAPBatchMeta deserialize(String source) {
        try {
            return mapper.readValue(source, IMAPBatchMeta.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistCalculations() {
        if (isDirty) {
            this.averageSize = sizeMean.evaluate();
            this.compressedAverageSize = compressedSizeMean.evaluate();

            this.medianSize = sizeMedian.evaluate(sizeMean.getData());
            this.medianCompressedSize = compressedSizeMedian
                    .evaluate(compressedSizeMean.getData());

            isDirty = false;
        }
    }

    public IMAPBatchMeta() {

    }

    public IMAPBatchMeta(IMAPMessageSource source, Date started) {
        super();
        this.setSource(source);
        this.started = started;
        this.sizeMean.setData(new double[]{});
        this.compressedSizeMean.setData(new double[]{});
    }

    public void incrementAttachmentStatistics(boolean hasAttachment) {
        if (hasAttachment) {
            messagesWithAttachments++;
        } else {
            messagesWithoutAttachments++;
        }
    }

    public void incrementBatchSize(long size) {
        batchSize += size;
    }

    public void incrementMessages() {
        this.messages++;
    }

    public void incrementFailures() {
        this.failures++;
    }

    public void incrementDuplicates() {
        this.duplicates++;
    }

    public void updateSizeStatistics(long size, long compressedSize) {
        isDirty = true;
        double[] sizes = Arrays.copyOf(sizeMean.getData(),
                sizeMean.getData().length + 1);
        sizes[sizeMean.getData().length] = size;
        sizeMean.setData(sizes);

        double[] csizes = Arrays.copyOf(compressedSizeMean.getData(),
                compressedSizeMean.getData().length + 1);
        csizes[compressedSizeMean.getData().length] = compressedSize;
        compressedSizeMean.setData(csizes);

        if (size > maxSize) {
            maxSize = size;
        }

        if (compressedSize > compressedMaxSize) {
            compressedMaxSize = compressedSize;
        }
    }

    public double getCompressedAverageSize() {
        return compressedAverageSize;
    }

    public long getCompressedMaxSize() {
        return compressedMaxSize;
    }

    public Date getStarted() {
        return started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public long getMessages() {
        return messages;
    }

    public long getDuplicates() {
        return duplicates;
    }

    public long getFailures() {
        return failures;
    }

    public double getAverageSize() {
        return averageSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getMessagesWithAttachments() {
        return messagesWithAttachments;
    }

    public long getMessagesWithoutAttachments() {
        return messagesWithoutAttachments;
    }

    public double getMedianSize() {
        return medianSize;
    }

    public double getMedianCompressedSize() {
        return medianCompressedSize;
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

    public IMAPMessageSource getSource() {
        return source;
    }

    public void setSource(IMAPMessageSource source) {
        this.source = source;
    }

    public long getBatchSize() {
        return batchSize;
    }

}
