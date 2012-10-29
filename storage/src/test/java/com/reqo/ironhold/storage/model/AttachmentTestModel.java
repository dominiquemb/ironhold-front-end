package com.reqo.ironhold.storage.model;

import java.io.IOException;

import org.easymock.EasyMock;

import com.pff.PSTAttachment;
import com.pff.PSTException;

public class AttachmentTestModel extends CommonTestModel {


	public static PSTAttachment generatePSTAttachment(int ordinal) throws PSTException, IOException {
		PSTAttachment testMessage = EasyMock.createMock(PSTAttachment.class);
		EasyMock.expect(testMessage.getAddrType()).andReturn((String) pickOne("SMTP", "EX","UNKNOWN"));
		EasyMock.expect(testMessage.getAttachmentContentDisposition()).andReturn(df.getRandomWord());
		EasyMock.expect(testMessage.getAttachMethod()).andReturn(df.getNumberBetween(0,6));
		EasyMock.expect(testMessage.getAttachNum()).andReturn(ordinal);
		EasyMock.expect(testMessage.getAttachSize()).andReturn(df.getNumberBetween(1, 100000));
		EasyMock.expect(testMessage.getComment()).andReturn(generateText());
		EasyMock.expect(testMessage.getContentId()).andReturn(df.getRandomWord());
		EasyMock.expect(testMessage.getCreationTime()).andReturn(df.getBirthDate());
		EasyMock.expect(testMessage.getDateItem(0)).andReturn(df.getBirthDate());
		EasyMock.expect(testMessage.getDisplayName()).andReturn(generateText());
		EasyMock.expect(testMessage.getEmailAddress()).andReturn(generateEmail());
		EasyMock.expect(testMessage.getFilename()).andReturn(df.getRandomWord() + ".pdf");
		EasyMock.expect(testMessage.getFilesize()).andReturn(df.getNumberBetween(1, 100000));
		EasyMock.expect(testMessage.getItemsString()).andReturn(generateText());
		EasyMock.expect(testMessage.getLastModificationTime()).andReturn(df.getBirthDate());
		EasyMock.expect(testMessage.getLongFilename()).andReturn(df.getRandomWord() + ".pdf");
		EasyMock.expect(testMessage.getLongPathname()).andReturn(df.getRandomWord() + ".pdf");
		EasyMock.expect(testMessage.getMessageClass()).andReturn(df.getRandomWord());
		EasyMock.expect(testMessage.getMimeSequence()).andReturn(df.getNumberBetween(1, 10));
		EasyMock.expect(testMessage.getMimeTag()).andReturn(df.getRandomWord());
		EasyMock.expect(testMessage.getPathname()).andReturn(df.getRandomWord() + ".pdf");
		EasyMock.expect(testMessage.getRenderingPosition()).andReturn(df.getNumberBetween(1, 10));
		EasyMock.expect(testMessage.getSize()).andReturn(df.getNumberBetween(1, 100000));
		
		return testMessage;
	}

}
