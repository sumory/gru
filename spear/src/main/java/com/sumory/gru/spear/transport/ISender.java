package com.sumory.gru.spear.transport;

import com.sumory.gru.spear.domain.MsgObject;

public interface ISender {

    public void run() throws Exception;

    public void send(final String topic, MsgObject msg);

    public void send(String topic, long tags, String body);
}
