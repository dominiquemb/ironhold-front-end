package com.reqo.ironhold.web.components;

import com.reqo.ironhold.model.message.pst.PSTFileMeta;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.List;

public class PSTStatisticsWindow extends Window {

	private final PSTStatisticsWindow me;
	private final IStorageService storageService;

	public PSTStatisticsWindow(IStorageService storageService) throws Exception {
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
