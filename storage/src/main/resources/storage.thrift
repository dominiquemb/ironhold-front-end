service MimeMailMessageStorage {
        i64 store(1: string clientName, 2: string partition, 3: string messageId, 4: string message, 5: string checkSum),
        bool exists(1: string clientName, 2: string partition, 3: string messageId),
        string get(1: string clientName, 2: string partition, 3: string messageId)
}