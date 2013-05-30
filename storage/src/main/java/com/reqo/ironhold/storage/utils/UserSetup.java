package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 3:04 PM
 */
public class UserSetup {

    @Autowired
    private MiscIndexService miscIndexService;

    public UserSetup() {

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Options bean = new Options();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println("Admin role: " + RoleEnum.SUPER_USER.getValue());
            return;
        }

        ApplicationContext context = new ClassPathXmlApplicationContext("utilities.xml");
        UserSetup client = context.getBean(UserSetup.class);
        client.generate(bean.getClient(), bean.getUsername(), bean.getPassword(), bean.getEmail(), bean.getRole());
        System.exit(1);
    }

    private void generate(String client, String username, String password, String email, int role) throws Exception {
        //     System.out.println(Integer.MAX_VALUE);
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setName(username);
        loginUser.setHashedPassword(CheckSumHelper.getCheckSum(password.getBytes()));
        loginUser.setRolesBitMask(role);
        Recipient recipient = new Recipient(username, email);
        loginUser.setMainRecipient(recipient);
        miscIndexService.store(client, loginUser);


    }

    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        @Option(name = "-username", usage = "username to setup", required = true)
        private String username;


        @Option(name = "-password", usage = "password to setup", required = true)
        private String password;

        @Option(name = "-email", usage = "email address", required = true)
        private String email;

        @Option(name = "-role", usage = "role integer bits", required = true)
        private int role;

        String getEmail() {
            return email;
        }

        int getRole() {
            return role;
        }

        public String getClient() {
            return client;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
