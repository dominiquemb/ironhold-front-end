package com.reqo.ironhold.web.components;

import java.util.List;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.PSTFileMeta;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class StatisticsWindow extends Window {

	private final StatisticsWindow me;
	private final IStorageService storageService;

	public StatisticsWindow(IStorageService storageService) throws Exception {
		super("Statistics");
		this.me = this;
		this.storageService = storageService;
		VerticalLayout layout = (VerticalLayout) getContent();
		this.setWidth("90%");
		this.setHeight("90%");
		layout.setMargin(true);
		layout.setSpacing(true);

		List<PSTFileMeta> pstFiles = storageService.getPSTFiles();
		for (PSTFileMeta pstFile : pstFiles) {
			PSTFileMetaPanel panel = new PSTFileMetaPanel(pstFile);
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
