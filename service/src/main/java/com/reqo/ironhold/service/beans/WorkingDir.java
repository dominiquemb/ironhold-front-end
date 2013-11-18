package com.reqo.ironhold.service.beans;

import org.springframework.stereotype.Component;

/**
 * User: ilya
 * Date: 10/27/13
 * Time: 10:56 PM
 */
@Component
public class WorkingDir {
    private String workDir;

    public WorkingDir(String workDir) {
        this.workDir = workDir;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
}