package com.reqo.ironhold.pstupload;

import com.reqo.ironhold.pstupload.frames.LoginFrame;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;

/**
 * User: ilya
 * Date: 10/26/13
 * Time: 8:06 AM
 */
public class Main {

    private static ClassPathXmlApplicationContext context;

    public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext("context.xml");


        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        LoginFrame loginFrame = context.getBean(LoginFrame.class);
        loginFrame.setVisible(true);

    }

}