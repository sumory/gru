package com.sumory.gru.spear.extention;

import com.corundumstudio.socketio.listener.DataListener;
import com.sumory.gru.spear.transport.IReceiver;

public abstract class ReceiverDataListener<T> implements DataListener<T> {

    private IReceiver receiver;

    public ReceiverDataListener(IReceiver receiver) {
        this.receiver = receiver;
    }

}
