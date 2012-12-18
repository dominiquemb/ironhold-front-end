package com.reqo.ironhold.storage.model;

public class MessageSourceTestModel extends CommonTestModel {

    public static PSTMessageSource generate() {
        return new PSTMessageSource(df.getName() + ".pst", df.getName(), (long) 10000 * (long) Math.random(),
                df.getBirthDate());
    }

}
