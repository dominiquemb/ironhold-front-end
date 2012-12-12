package com.reqo.ironhold.storage.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class PSTFileMeta {
	private String pstFileName = StringUtils.EMPTY;
	private String mailBoxName = StringUtils.EMPTY;
	private String originalFilePath = StringUtils.EMPTY;
	private String commentary = StringUtils.EMPTY;
	private String md5;
	private long size;
	private Date started;
	private Date finished;
	private long messages;
	private long duplicates;
	private long failures;
	private long partialFailures;

	private long maxSize;
	private long compressedMaxSize;

	private long maxAttachmentSize;
	private long compressedMaxAttachmentSize;

	private long messagesWithAttachments;
	private long messagesWithoutAttachments;
	private Map<String, Long> typeMap = new HashMap<String, Long>();
	private Map<String, Long> folderMap = new HashMap<String, Long>();

	@JsonIgnore
	private Mean sizeMean = new Mean();
	@JsonIgnore
	private Mean attachmentSizeMean = new Mean();
	@JsonIgnore
	private Median sizeMedian = new Median();
	@JsonIgnore
	private Median attachmentSizeMedian = new Median();
	@JsonIgnore
	private Mean compressedSizeMean = new Mean();
	@JsonIgnore
	private Mean compressedAttachmentSizeMean = new Mean();
	@JsonIgnore
	private Median compressedSizeMedian = new Median();
	@JsonIgnore
	private Median compressedAttachmentSizeMedian = new Median();
	private double compressedAverageSize;
	private double compressedAverageAttachmentSize;
	private double averageSize;
	private double averageAttachmentSize;
	private double medianSize;
	private double medianAttachmentSize;
	private double medianCompressedSize;
	private double medianCompressedAttachmentSize;
	@JsonIgnore
	private boolean isDirty = false;

	private static ObjectMapper mapper = new ObjectMapper();

	public static String toJSON(PSTFileMeta meta)
			throws JsonGenerationException, JsonMappingException, IOException {
		meta.persistCalculations();
		return mapper.writeValueAsString(meta);
	}

	public void persistCalculations() {
		if (isDirty) {
			this.averageSize = sizeMean.evaluate();
			this.averageAttachmentSize = attachmentSizeMean.evaluate();
			this.compressedAverageSize = compressedSizeMean.evaluate();
			this.compressedAverageAttachmentSize = compressedAttachmentSizeMean
					.evaluate();

			this.medianSize = sizeMedian.evaluate(sizeMean.getData());
			this.medianAttachmentSize = attachmentSizeMedian
					.evaluate(attachmentSizeMean.getData());
			this.medianCompressedSize = compressedSizeMedian
					.evaluate(compressedSizeMean.getData());
			this.medianCompressedAttachmentSize = compressedAttachmentSizeMedian
					.evaluate(compressedAttachmentSizeMean.getData());
			
			isDirty = false;
		}
	}

	public static PSTFileMeta fromJSON(String meta) throws JsonParseException,
			JsonMappingException, IOException {
		return mapper.readValue(meta, PSTFileMeta.class);
	}

	protected PSTFileMeta() {

	}

	public PSTFileMeta(String pstFileName, String mailBoxName,
			String originalFilePath, String commentary, String md5, long size,
			Date started) {
		super();
		this.pstFileName = pstFileName;
		this.mailBoxName = mailBoxName;
		this.originalFilePath = originalFilePath;
		this.commentary = commentary;
		this.md5 = md5;
		this.size = size;
		this.started = started;
		this.sizeMean.setData(new double[] {});
		this.attachmentSizeMean.setData(new double[] {});
		this.compressedSizeMean.setData(new double[] {});
		this.compressedAttachmentSizeMean.setData(new double[] {});
	}

	public void addFolder(String folderPath, int contentCount) {
		folderMap.put(folderPath, new Long(contentCount));
	}

	public void incrementAttachmentStatistics(boolean hasAttachment) {
		if (hasAttachment) {
			messagesWithAttachments++;
		} else {
			messagesWithoutAttachments++;
		}
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

	public void incrementPartialFailures() {
		this.partialFailures++;
	}

	public void incrementObjectType(String type) {
		if (typeMap.containsKey(type)) {
			typeMap.put(type, typeMap.get(type).longValue() + 1L);
		} else {
			typeMap.put(type, 1L);
		}
	}

	public void updateSizeStatistics(int size, int compressedSize) {
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

	public void updateAttachmentSizeStatistics(int size, int compressedSize) {
		isDirty = true;
		double[] sizes = Arrays.copyOf(attachmentSizeMean.getData(),
				attachmentSizeMean.getData().length + 1);
		sizes[attachmentSizeMean.getData().length] = size;
		attachmentSizeMean.setData(sizes);

		double[] csizes = Arrays.copyOf(compressedAttachmentSizeMean.getData(),
				compressedAttachmentSizeMean.getData().length + 1);
		csizes[compressedAttachmentSizeMean.getData().length] = compressedSize;
		compressedAttachmentSizeMean.setData(csizes);

		if (size > maxAttachmentSize) {
			maxAttachmentSize = size;
		}

		if (compressedSize > compressedMaxAttachmentSize) {
			compressedMaxAttachmentSize = compressedSize;
		}
	}

	public double getCompressedAverageSize() {
		return compressedAverageSize;
	}

	public long getCompressedMaxSize() {
		return compressedMaxSize;
	}

	public double getCompressedAverageAttachmentSize() {
		return compressedAverageAttachmentSize;
	}

	public long getCompressedMaxAttachmentSize() {
		return compressedMaxAttachmentSize;
	}

	public long getPartialFailures() {
		return partialFailures;
	}

	public String getPstFileName() {
		return pstFileName;
	}

	public String getMailBoxName() {
		return mailBoxName;
	}

	public String getOriginalFilePath() {
		return originalFilePath;
	}

	public String getMd5() {
		return md5;
	}

	public long getSize() {
		return size;
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

	public double getAverageAttachmentSize() {
		return averageAttachmentSize;
	}

	public long getMaxAttachmentSize() {
		return maxAttachmentSize;
	}

	public long getMessagesWithAttachments() {
		return messagesWithAttachments;
	}

	public long getMessagesWithoutAttachments() {
		return messagesWithoutAttachments;
	}

	public String getCommentary() {
		return commentary;
	}

	public double getMedianSize() {
		return medianSize;
	}

	public double getMedianAttachmentSize() {
		return medianAttachmentSize;
	}

	public double getMedianCompressedSize() {
		return medianCompressedSize;
	}

	public double getMedianCompressedAttachmentSize() {
		return medianCompressedAttachmentSize;
	}

	public Map<String, Long> getTypeMap() {
		return typeMap;
	}

	public Map<String, Long> getFolderMap() {
		return folderMap;
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

}
