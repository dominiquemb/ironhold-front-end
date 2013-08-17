package com.reqo.ironhold.importer.watcher;

import org.kohsuke.args4j.Option;

/**
 * User: ilya
 * Date: 8/3/13
 * Time: 11:59 PM
 */
public class QueueWatcherOptions extends WatcherOptions {
    @Option(name = "-ignoreAttList", usage = "ignore message ids for extraction of text from attachments", required = false)
    private String ignoreAttachmentExtractList;

    public String getIgnoreAttachmentExtractList() {
        return ignoreAttachmentExtractList;
    }
}