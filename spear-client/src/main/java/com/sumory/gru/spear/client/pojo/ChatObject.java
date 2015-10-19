package com.sumory.gru.spear.client.pojo;

import java.io.Serializable;

public class ChatObject implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String userName;
    private String message;

    public ChatObject() {
    }

    public ChatObject(String userName, String message) {
        super();
        this.userName = userName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
