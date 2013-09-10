package org.syxc.util;

public enum DateFormater {

    NORMAL("yyyy-MM-dd HH:mm"),
    DD("yyyy-MM-dd"),
    SS("yyyy-MM-dd HH:mm:ss");

    private String value;

    private DateFormater(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
