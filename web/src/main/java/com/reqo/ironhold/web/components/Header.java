package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class Header extends HorizontalLayout {
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G");

	public Header(final Application application, final String username,
			final IStorageService storageService) {
		this.setWidth("100%");
		setSizeFull();

		// Button logo = new Button();
		// logo.setStyleName(BaseTheme.BUTTON_LINK);
		// logo.setIcon(new ClassResource("images/logo.png", application));
		//
		// this.addComponent(logo);
		//
		// this.setComponentAlignment(logo, Alignment.TOP_LEFT);

		VerticalLayout vl = new VerticalLayout();
		Button statistics = new Button("Statistics");
		statistics.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				StatisticsWindow window;
				try {
					window = new StatisticsWindow(storageService);
					window.setModal(true);
					getWindow().addWindow(window);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		Button logout = new Button("Logout");
		logout.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {

				getApplication().close();

			}
		});
		vl.setSizeFull();
		vl.setWidth("200");
		Label label = new Label("Welcome, " + username);
		label.setWidth(null);

		vl.addComponent(label);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.addComponent(statistics);
		hl.addComponent(logout);

		vl.addComponent(hl);

		this.addComponent(vl);
		this.setComponentAlignment(vl, Alignment.TOP_RIGHT);
	}
}
