service com.reqo.ironhold.storage.thrift.MimeMailMessageStorage {
        i32 store(1: string message),
        bool exists(1: string messageId),
        string get(1: string messageId)
}