service MimeMailMessageStorage {
        i64 store(1: string clientName, 2: string partition, 3: string subPartition, 4: string messageId, 5: string message, 6: string checkSum),
        bool exists(1: string clientName, 2: string partition, 3: string subPartition, 4: string messageId),
        string get(1: string clientName, 2: string partition, 3: string subPartition, 4: string messageId)
}