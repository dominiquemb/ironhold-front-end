package com.reqo.ironhold.storage.model.search;

import com.reqo.ironhold.web.domain.interfaces.IHasMessageId;
import com.reqo.ironhold.web.domain.interfaces.IPartitioned;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * User: ilya
 * Date: 3/28/13
 * Time: 8:54 AM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexFailure implements IHasMessageId, IPartitioned {
    @JsonIgnore
    private ObjectMapper mapper = new ObjectMapper();
    private String messageId;
    private String partition;
    private String exceptionText;

    public IndexFailure() {

    }

    public IndexFailure(String messageId, String partition, Exception exception) {
        this.messageId = messageId;
        this.partition = partition;
        this.exceptionText = exception.getMessage();
    }

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public IndexFailure deserialize(String source) throws IOException {
        return mapper.readValue(source, IndexFailure.class);
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public String getPartition() {
        return partition;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }


}
