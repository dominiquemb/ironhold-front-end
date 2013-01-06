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
	

	public RandomTextGenerator() {
		loadParagraphs("2600.txt");
		loadParagraphs("2320.txt");
		loadParagraphs("25606-0.txt");
	}


	private void loadParagraphs(String fileName) {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(fileName);

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
	
}