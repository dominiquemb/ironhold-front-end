package com.reqo.ironhold.demodata;

import java.io.InputStream;

import java.util.ArrayList;

import java.util.List;

import java.util.Scanner;

public class RandomTextGenerator {

	public String generate() {
		int offset = (int) (Math.random() * (paragraphs.size() - 20));
		int number = (int) (Math.random() * 15) + 5;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < number; i++) {
			sb.append(paragraphs.get(offset + i));
		}

		return sb.toString();

	}
	
	public String generateName() {
		int number = (int) (Math.random()*names.size());
		return names.get(number);
	}

	public RandomTextGenerator() {
		loadParagraphs();
		loadNames();

	}

	private void loadNames() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream("2600-names.txt");

		Scanner scanner = new Scanner(in).useDelimiter("\n");

		StringBuffer paragraph = new StringBuffer();

		while (scanner.hasNext()) {
			String nextLine = scanner.next();
			if (nextLine.trim().length() > 0) {
				
				names.add(nextLine.replace("\r", ""));
			}
		}
	}

	private void loadParagraphs() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream("2600.txt");

		Scanner scanner = new Scanner(in).useDelimiter("\n");

		StringBuffer paragraph = new StringBuffer();

		while (scanner.hasNext()) {

			String nextLine = scanner.next();

			paragraph.append(nextLine);
			paragraph.append("\n");

			if (nextLine.trim().length() == 0
					&& paragraph.toString().trim().length() > 0) {

				if (paragraph.toString().trim().length() > 15) {
					paragraphs.add(paragraph.toString());
				}

				paragraph = new StringBuffer();

			}

		}

	}

	private List<String> paragraphs = new ArrayList<String>();
	private List<String> names = new ArrayList<String>();

}