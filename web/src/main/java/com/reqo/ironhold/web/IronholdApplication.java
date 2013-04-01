package com.reqo.ironhold.web;

import com.reqo.ironhold.web.components.SearchWindow;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Theme(value = "ironhold")
@Scope("prototype")
@Component
public class IronholdApplication extends UI {
    private static Logger logger = Logger.getLogger(IronholdApplication.class);

    @Autowired
    private SearchWindow searchWindow;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        this.addWindow(searchWindow);
       /* final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        layout.addComponent(searchWindow);      */
        searchWindow.show();
    }
}
