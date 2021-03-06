package com.reqo.ironhold.storage.thrift;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;

/**
 * User: ilya
 * Date: 4/2/13
 * Time: 9:18 PM
 */
public class MimeMailMessageStorageClient implements IMimeMailMessageStorageService {
    private static Logger logger = Logger.getLogger(MimeMailMessageStorageClient.class);

    private MimeMailMessageStorage.Client client;
    private String host;
    private int port;
    private TTransport transport;

    public MimeMailMessageStorageClient(String host, int port) throws TTransportException {
        this.host = host;
        this.port = port;

        reconnect();


    }

    public void reconnect() throws TTransportException {
        if (transport != null) {
            logger.info("Attempting to reconnect to storage service");
            try {
                transport.close();
            } catch (Exception e) {
                //ignore
            }
        }
        transport = new TSocket(host, port, 3000000);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        client = new MimeMailMessageStorage.Client(protocol);

    }

    @Override
    public long store(String client, String partition, String subPartition, String messageId, String serializedMailMessage, String checkSum, boolean encrypt) throws Exception {
        return this.client.store(client, partition, subPartition, messageId, serializedMailMessage, checkSum, encrypt);
    }

    @Override
    public boolean exists(String client, String partition, String subPartition, String messageId) throws Exception {
        return this.client.exists(client, partition, subPartition, messageId);
    }

    @Override
    public boolean isEncrypted(String client, String partition, String subPartition, String messageId) throws Exception {
        return this.client.isEncrypted(client, partition, subPartition, messageId);
    }


    @Override
    public String get(String client, String partition, String subPartition, String messageId) throws Exception {
        return this.client.get(client, partition, subPartition, messageId);
    }

    @Override
    public List<String> getPartitions(String clientName) throws Exception {
        return this.client.getPartitions(clientName);
    }

    @Override
    public List<String> getSubPartitions(String clientName, String partition) throws Exception {
        return this.client.getSubPartitions(clientName, partition);
    }

    @Override
    public List<String> getList(String clientName, String partition, String subPartition) throws Exception {
        return this.client.getList(clientName, partition, subPartition);
    }

    @Override
    public boolean archive(String clientName, String partition, String subPartition, String messageId) throws Exception {
        return this.client.archive(clientName, partition, subPartition, messageId);
    }
}
