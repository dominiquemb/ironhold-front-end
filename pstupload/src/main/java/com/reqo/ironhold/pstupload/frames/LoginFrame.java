package com.reqo.ironhold.pstupload.frames;

import javax.swing.*;

/**
 * User: ilya
 * Date: 10/26/13
 * Time: 8:30 AM
 */
public class LoginFrame extends JFrame {
    public LoginFrame() {
        super("IronHold PST Upload");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        getContentPane().add(label);


    }
}
