package com.reqo.ironhold.web.components;

import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class Header extends HorizontalLayout {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G");
    private static Logger logger = Logger.getLogger(Header.class);

    public Header(final String username) {
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
/*		Button pstStatistics = new Button("PST Stats");
        pstStatistics.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				PSTStatisticsWindow window;
				try {
					window = new PSTStatisticsWindow(storageService);
					window.setModal(true);
					getWindow().addWindow(window);

				} catch (Exception e) {
					logger.warn(e);
				}

			}
		});

		Button imapStatistics = new Button("IMAP Stats");
		imapStatistics.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				IMAPStatisticsWindow window;
				try {
					window = new IMAPStatisticsWindow(storageService);
					window.setModal(true);
					getWindow().addWindow(window);

				} catch (Exception e) {
					logger.warn(e);
				}

			}
		});
*/
        Button logout = new Button("Logout");
        logout.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                UI.getCurrent().getSession().close();
                Page.getCurrent().setLocation(Page.getCurrent().getLocation());
            }
        });
        vl.setSizeFull();
        vl.setWidth("300");
        Label label = new Label("Welcome, " + username);
        label.setWidth(null);

        vl.addComponent(label);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        //	hl.addComponent(pstStatistics);
        //hl.addComponent(imapStatistics);
        hl.addComponent(logout);

        vl.addComponent(hl);

        this.addComponent(vl);
        this.setComponentAlignment(vl, Alignment.TOP_RIGHT);
    }
}
