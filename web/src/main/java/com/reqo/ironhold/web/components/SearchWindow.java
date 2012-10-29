package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexService;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SearchWindow extends Window {

	public SearchWindow(String title) {
		super(title);
		
		final IndexService indexService = new IndexService("reqo");

		final TextField filterField = new TextField("Search criteria");
		final Button searchButton = new Button("Search");

		final Label previewLabel = new Label();

		filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		filterField.setTextChangeTimeout(200);
		filterField.addListener(new TextChangeListener() {

			public void textChange(TextChangeEvent event) {
				long results = indexService.getMatches(event.getText());
				previewLabel.setCaption("Results: " + results);
			}
		});

		addComponent(filterField);
		addComponent(previewLabel);
		addComponent(searchButton);
		filterField.setWidth("150px");
	}
}
