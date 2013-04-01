package com.reqo.ironhold.web.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * User: ilya
 * Date: 3/30/13
 * Time: 10:38 PM
 */
public class FacetPanel extends Panel {

    private final VerticalLayout layout;

    public FacetPanel(String caption) {
        super(caption);
        layout = new VerticalLayout();
        layout.setMargin(true);
    }

    public void removeAllComponents() {
        layout.removeAllComponents();
    }

    public void addComponent(Component component) {
        layout.addComponent(component);
    }
}
