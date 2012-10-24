package com.reqo.ironhold.storage.model;

import java.util.ArrayList;
import java.util.List;

public class AttachmentTestModel extends CommonTestModel {

	public static Attachment generateAttachment() {
		Attachment attachment = new Attachment();

		attachment.setBody(generateText());
		attachment.setFileName(df.getRandomWord() + ".pdf");

		return attachment;
	}

	public static List<Attachment> generateAttachments() {
		int n = (int) (10 * Math.random());
		List<Attachment> attachments = new ArrayList<Attachment>();
		for (int i = 0; i < n; i++) {
			attachments.add(generateAttachment());
		}
		
		return attachments;
	}

}
