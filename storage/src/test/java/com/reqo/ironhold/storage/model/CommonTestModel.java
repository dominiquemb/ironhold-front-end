package com.reqo.ironhold.storage.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.fluttercode.datafactory.impl.DataFactory;

public class CommonTestModel {
	protected static DataFactory df = new DataFactory();

	protected static List<String> generateNames() {
		DataFactory df = new DataFactory();
		int n = (int) (10 * Math.random());
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			names.add(df.getName());
		}

		return names;
	}

	protected static String generateText() {
		DataFactory df = new DataFactory();
		int n = (int) (10 * Math.random());
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			names.add(df.getName());
		}

		return StringUtils.join(names, ' ');
	}

	protected static Date getMinDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(1990, 1, 1, 9, 0, 0);

		return cal.getTime();
	}
	
	protected static Date getMaxDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(2025, 1, 1, 9, 0, 0);

		return cal.getTime();
	}
}
