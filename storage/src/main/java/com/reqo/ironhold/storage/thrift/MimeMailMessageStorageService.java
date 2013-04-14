package com.reqo.ironhold.storage.thrift;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * User: ilya
 * Date: 4/2/13
 * Time: 9:09 PM
 */
public class MimeMailMessageStorageService {
    public static void StartsimpleServer(MimeMailMessageStorage.Processor<MimeMailMessageStorageServiceHandler> processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);

            TServer server = new TThreadPoolServer(new
                    TThreadPoolServer.Args(serverTransport).processor(processor));

            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("mimeMailMessageStorageServiceContext.xml");

        StartsimpleServer(new MimeMailMessageStorage.Processor<MimeMailMessageStorageServiceHandler>(context.getBean(MimeMailMessageStorageServiceHandler.class)));
    }
}
