package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SearchBar extends VerticalLayout {

	private SearchTextField filterField;

	public SearchBar(final String indexPrefix, final Window window, final MessageIndexService messageIndexService,
			final IStorageService storageService, SearchResults searchResults) {

		filterField = new SearchTextField(window, searchResults);

		final Label previewLabel = new Label(String.format(
				"%,d total messages", storageService.getTotalMessageCount()));

		filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		filterField.setTextChangeTimeout(200);
		filterField.setWidth("400px");
		filterField.addListener(new TextChangeListener() {

			public void textChange(TextChangeEvent event) {
				String criteria = event.getText();
				if (criteria.trim().length() > 0) {
					long results = messageIndexService.getMatchCount(indexPrefix, event.getText());
					if (results >= 0) {
						previewLabel.setValue(String.format(
								"%,d matched messages", results));
					} else {
						previewLabel.setValue("Invalid search query");
					}
				} else {
					previewLabel.setValue(String.format("%,d total messages",
							storageService.getTotalMessageCount()));
				}
			}
		});
		this.addComponent(filterField);
		this.addComponent(previewLabel);

		filterField.focus();
	}

	public String getCriteria() {
		return (String) filterField.getValue();
	}

}
