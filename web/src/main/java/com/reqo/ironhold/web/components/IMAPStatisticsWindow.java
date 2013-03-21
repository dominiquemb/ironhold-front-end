package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.List;

public class IMAPStatisticsWindow extends Window {

	private final IMAPStatisticsWindow me;
	private final IStorageService storageService;

	public IMAPStatisticsWindow(IStorageService storageService) throws Exception {
		super("Statistics");
		this.me = this;
		this.storageService = storageService;
		VerticalLayout layout = (VerticalLayout) getContent();
		this.setWidth("90%");
		this.setHeight("90%");
		layout.setMargin(true);
		layout.setSpacing(true);

		List<IMAPBatchMeta> imapBatches = storageService.getIMAPBatches();
		for (IMAPBatchMeta imapBatch : imapBatches) {
			IMAPBatchMetaPanel panel = new IMAPBatchMetaPanel(imapBatch);
			layout.addComponent(panel);
		}
		Button close = new Button("Close", new Button.ClickListener() {
			// inline click-listener
			public void buttonClick(ClickEvent event) {
				// close the window by removing it from the parent window
				(getParent()).removeWindow(me);
			}
		});

		layout.addComponent(close);
		layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
	}

}
