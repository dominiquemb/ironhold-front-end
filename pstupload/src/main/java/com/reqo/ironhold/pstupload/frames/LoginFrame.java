package com.reqo.ironhold.pstupload.frames;

import com.reqo.ironhold.pstupload.utils.SpringUtilities;
import com.reqo.ironhold.uploadclient.LoginClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

/**
 * User: ilya
 * Date: 10/26/13
 * Time: 8:30 AM
 */
@Component
public class LoginFrame extends JFrame {

    @Autowired
    private LoginClient loginClient;

    public LoginFrame() {
        super("IronHold PST Upload");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);


        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel fieldPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        fieldPanel.setLayout(layout);

        JLabel clientKeyLabel = new JLabel("Client Key:", JLabel.TRAILING);
        fieldPanel.add(clientKeyLabel);
        JTextField clientKeyField = new JTextField(10);
        clientKeyLabel.setLabelFor(clientKeyField);
        fieldPanel.add(clientKeyField);

        JLabel usernameLabel = new JLabel("Username:", JLabel.TRAILING);
        fieldPanel.add(usernameLabel);
        JTextField usernameField = new JTextField(10);
        usernameLabel.setLabelFor(usernameField);
        fieldPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:", JLabel.TRAILING);
        fieldPanel.add(passwordLabel);
        JTextField passwordField = new JPasswordField(10);
        passwordLabel.setLabelFor(passwordField);
        fieldPanel.add(passwordField);


        SpringUtilities.makeCompactGrid(fieldPanel,
                3, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        contentPane.add(fieldPanel);

        JButton loginButton = new JButton("Login");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.anchor = GridBagConstraints.CENTER; //bottom of space
        c.insets = new Insets(10,30,10,30);  //top padding
        c.gridx = 0;       //aligned with button 2
        c.gridwidth = 1;   //2 columns wide
        c.gridy = 1;       //third row

        contentPane.add(loginButton, c);

        pack();

    }


    public static void main(String[] args) {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");


        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginFrame loginFrame = context.getBean(LoginFrame.class);
                loginFrame.setVisible(true);
            }
        });
    }


}
