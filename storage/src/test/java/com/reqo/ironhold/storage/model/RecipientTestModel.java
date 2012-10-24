package com.reqo.ironhold.storage.model;

import java.util.ArrayList;
import java.util.List;

public class RecipientTestModel extends CommonTestModel {

	public static List<Recipient> generateRecipients() {
		List<Recipient> recipients = new ArrayList<Recipient>();
		for (String name : generateNames()) {
			recipients.add(new Recipient(name, name, name));
		}
		
		return recipients;
	}

}
