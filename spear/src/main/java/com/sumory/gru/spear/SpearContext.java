package com.sumory.gru.spear;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.sumory.gru.common.config.Config;
import com.sumory.gru.idgen.service.IdService;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.spear.transport.IReceiver;
import com.sumory.gru.spear.transport.ISender;
import com.sumory.gru.stat.service.StatService;

/**
 * spear上下文
 * 
 * @author sumory.wu
 * @date 2015年3月23日 下午6:42:49
 */
public class SpearContext {

    private static ConcurrentHashMap<String, Group> groupMap;//以群组id作为key
    private static ConcurrentHashMap<String, User> userMap;//以“用户类型_用户id”作为key
    private static Map<String, String> config;//全局配置
    private IdService idService;
    private StatService statService;
    private ISender sender;
    private IReceiver receiver;
    private BlockingQueue<MsgObject> msgQueue;//存放消息的队列，用于单节点时进程内部传输消息使用

    private SpearContext() {
        groupMap = new ConcurrentHashMap<>();
        userMap = new ConcurrentHashMap<>();
        msgQueue = new LinkedBlockingDeque<MsgObject>();
        config = Config.getConfig();
    }

    public ISender getSender() {
        return sender;
    }

    public void setSender(ISender sender) {
        this.sender = sender;
    }

    public IReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(IReceiver receiver) {
        this.receiver = receiver;
    }

    public IdService getIdService() {
        return idService;
    }

    public void setIdService(IdService idService) {
        this.idService = idService;
    }

    public StatService getStatService() {
        return statService;
    }

    public void setStatService(StatService statService) {
        this.statService = statService;
    }

    private static class SingletonHolder {
        final static SpearContext instance = new SpearContext();
    }

    public static SpearContext getInstance() {
        return SingletonHolder.instance;
    }

    public ConcurrentHashMap<String, Group> getGroupMap() {
        return groupMap;
    }

    public ConcurrentHashMap<String, User> getUserMap() {
        return userMap;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public BlockingQueue<MsgObject> getMsgQueue() {
        return msgQueue;
    }

}