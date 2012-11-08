package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SearchBar extends VerticalLayout {

	private SearchTextField filterField;

	public SearchBar(final Window window, final IndexService indexService,
			final IStorageService storageService, SearchResults searchResults) {

		filterField = new SearchTextField(window, searchResults);

		final Label previewLabel = new Label(Long.toString(storageService
				.getTotalCount()) + " total messages");

		filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		filterField.setTextChangeTimeout(200);
		filterField.setWidth("300px");
		filterField.addListener(new TextChangeListener() {

			public void textChange(TextChangeEvent event) {
				String criteria = event.getText();
				if (criteria.trim().length() > 0) {
					long results = indexService.getMatchCount(event.getText());
					previewLabel.setValue("Results: " + results);
				} else {
					previewLabel.setValue(Long.toString(storageService
							.getTotalCount()) + " total messages");
				}
			}
		});
		this.addComponent(filterField);
		this.addComponent(previewLabel);
	}

	public String getCriteria() {
		return (String) filterField.getValue();
	}

}
