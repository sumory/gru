package com.sumory.gru.spear.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.common.utils.BitSetUtil;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.stat.service.StatService;

/**
 * 统计本节点基本信息(如在线人数)，通过定时任务同步到stat->redis
 * 需优化redis，大量的写，考虑多线程同步状态
 * 
 * @author sumory.wu
 * @date 2015年3月23日 下午3:06:39
 */
public class UserStat {
    private final static Logger logger = LoggerFactory.getLogger(UserStat.class);

    private AtomicInteger count = new AtomicInteger(0);
    private SpearContext context;
    private StatService statService;
    private String spearId;
    private String node;

    public UserStat(SpearContext context) {
        this.context = context;
        this.statService = context.getStatService();
        this.spearId = context.getConfig().get("spear.id");
        this.node = "spear" + this.spearId;
    }

    @Deprecated
    private void excuteStatNormal() {
        ConcurrentHashMap<String, Group> groupMap = context.getGroupMap();

        if (!groupMap.isEmpty()) {
            Iterator<Entry<String, Group>> entries = groupMap.entrySet().iterator();
            Map<String, String> stats = new HashMap<String, String>();
            while (entries.hasNext()) {
                Entry<String, Group> entry = entries.next();
                String groupId = entry.getKey();
                Group group = entry.getValue();
                ConcurrentLinkedQueue<User> users = group.getUsers();

                if (!users.isEmpty()) {

                    StringBuilder sb = new StringBuilder();
                    Iterator<User> it = users.iterator();
                    while (it.hasNext()) {
                        User u = it.next();
                        sb.append(u.getId()).append(",");
                    }
                    String userIds = sb.toString();
                    userIds = StringUtils.trimTrailingCharacter(userIds, ',');
                    userIds = StringUtils.trimLeadingCharacter(userIds, ',');
                    stats.put(groupId, userIds);
                }
            }
            //System.out.println(JSON.toJSONString(stats));

            int everySendCount = 15;
            List<String> once = new ArrayList<String>(everySendCount);
            if (!stats.isEmpty()) {//存入redis
                Iterator<String> it = stats.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {//需优化，分批发送
                    String groupId = it.next();
                    i++;
                    once.add("spear" + UserStat.this.spearId + "_" + groupId + "_"
                            + stats.get(groupId));
                    if (i >= everySendCount) {
                        UserStat.this.statService.setGroupStatOfNode(once, 20);
                        i = 0;
                        once.clear();
                    }
                }
                UserStat.this.statService.setGroupStatOfNode(once, 20);
            }
        }
    }

    /**
     * 执行统计上报
     * 
     * @author sumory.wu @date 2015年4月22日 下午6:46:00
     */
    private void excuteStat() {
        ConcurrentHashMap<String, Group> groupMap = this.context.getGroupMap();
        List<StatObject> stats = new ArrayList<>(groupMap.size());

        if (!groupMap.isEmpty()) {
            Iterator<Entry<String, Group>> entries = groupMap.entrySet().iterator();
            while (entries.hasNext()) {
                Entry<String, Group> entry = entries.next();
                Long groupId = Long.parseLong(entry.getKey());
                Group group = entry.getValue();
                ConcurrentLinkedQueue<User> users = group.getUsers();

                if (!users.isEmpty()) {
                    List<Integer> userIds = new ArrayList<>();//业务上id不会超过Integer上限
                    Iterator<User> it = users.iterator();
                    while (it.hasNext()) {
                        User u = it.next();
                        userIds.add(new Long(u.getId()).intValue());
                    }

                    //userIds不为空
                    StatObject statObject = BitSetUtil.buildFrom(groupId, userIds, "");
                    stats.add(statObject);
                }
            }

            if (!stats.isEmpty()) {//存入redis
                int everySendCount = 20;
                List<StatObject> once = new ArrayList<StatObject>(everySendCount);
                int count = 0;
                for (int s = 0; s < stats.size(); s++) {//分批发送
                    once.add(stats.get(s));
                    count++;
                    if (count >= everySendCount) {
                        UserStat.this.statService.setGroupStatObjectOfNode(this.node, once, 20);
                        count = 0;
                        once.clear();
                    }
                }
                UserStat.this.statService.setGroupStatObjectOfNode(this.node, once, 20);
            }
        }
    }

    //定时任务
    public void run() {
        if ("true".equals(this.context.getConfig().get("stat.start"))) {//如果开启了统计服务
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        int number = count.addAndGet(1);
                        logger.info("执行第{}次上报.", number);
                        if (number >= Integer.MAX_VALUE)
                            count.set(0);

                        excuteStat();//上报各群组信息

                        logger.info("当前该节点总人数{}", context.getUserMap().size());
                        UserStat.this.statService.setUserCountOfNode(UserStat.this.node, context
                                .getUserMap().size() + "", 20);
                        logger.info("执行第{}次上报结束.", number);
                    }
                    catch (Exception e) {//捕捉异常，否则定时任务将终止
                        logger.error("定时上报任务发生异常", e);
                        //throw e;
                    }
                }
            }, 0, 10, TimeUnit.SECONDS);//每隔10s往redis发送一次数据统计
        }
    }
}
