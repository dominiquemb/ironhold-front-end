package com.reqo.ironhold.web.components.pagingcomponent.utilities;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class Utils {

    /**
     * @param string
     * @return <code>true</code> if the string is <code>null</code> or empty
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Adds the component into the container if the component isn't <code>null</code>.
     *
     * @param container
     * @param component
     */
    public static void addComponent(ComponentContainer container, Component component) {
        if (component != null) {
            container.addComponent(component);
        }
    }

    /**
     * Call the {@link Button#addListener(com.vaadin.ui.Button.ClickListener)} method on the button if this one isn't <code>null</code>.
     *
     * @param button
     * @param listener
     */
    public static void addListener(Button button, ClickListener listener) {
        if (button != null) {
            button.addClickListener(listener);
        }
    }

    /**
     * Call the {@link Component#setStyleName(String)} method on the component if this one isn't <code>null</code>.
     *
     * @param component
     * @param styleName
     */
    public static void setStyleName(Component component, String styleName) {
        if (component != null) {
            component.setStyleName(styleName);
        }
    }

    /**
     * Call the {@link Component#addStyleName(String)} method on the component if this one isn't <code>null</code>.
     *
     * @param component
     * @param styleName
     */
    public static void addStyleName(Component component, String styleName) {
        if (component != null) {
            component.addStyleName(styleName);
        }
    }

    /**
     * Call the {@link Component#removeStyleName(String)} method on the component if this one isn't <code>null</code>.
     *
     * @param component
     * @param styleName
     */
    public static void removeStyleName(Component component, String styleName) {
        if (component != null) {
            component.removeStyleName(styleName);
        }
    }

}
