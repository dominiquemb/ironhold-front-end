package com.reqo.ironhold.storage.thrift;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * User: ilya
 * Date: 4/2/13
 * Time: 9:18 PM
 */
public class MimeMailMessageStorageClient implements IMimeMailMessageStorageService {

    private final MimeMailMessageStorage.Client client;

    public MimeMailMessageStorageClient(String host, int port) throws TTransportException {
        TTransport transport;

        transport = new TSocket(host, port);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        client = new MimeMailMessageStorage.Client(protocol);


    }

    @Override
    public long store(String client, String partition, String subPartition, String messageId, String serializedMailMessage, String checkSum) throws Exception {
        return this.client.store(client, partition, subPartition, messageId, serializedMailMessage, checkSum);
    }

    @Override
    public boolean exists(String client, String partition, String subPartition, String messageId) throws Exception {
        return this.client.exists(client, partition, subPartition, messageId);
    }

    @Override
    public String get(String client, String partition, String subPartition, String messageId) throws Exception {
        return this.client.get(client, partition, subPartition, messageId);
    }
}
