package com.sumory.gru.spear.extention;

import com.corundumstudio.socketio.listener.DataListener;
import com.sumory.gru.spear.transport.ISender;

public abstract class SenderDataListener<T> implements DataListener<T> {

    private ISender sender;

    public SenderDataListener(ISender sender) {
        this.sender = sender;
    }

}
