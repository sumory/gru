package com.sumory.gru.spear.client.msg;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

/**
 * 压测消息发送（上行）客户端
 * 
 * @author sumory.wu
 * @date 2015年4月14日 下午7:00:49
 */
public class MsgMain {
    private static Logger logger = LoggerFactory.getLogger(MsgMain.class);
    private static ConcurrentHashMap<Integer, Socket> sockets = new ConcurrentHashMap<Integer, Socket>();
    private static List<Integer> userIds = new ArrayList<Integer>();

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        if (args.length >= 3) {

            String url = args[0];//长连接地址
            int sendTick = Integer.parseInt(args[1]);//多久触发一次发消息操作，ms
            int sendCountEveryTick = Integer.parseInt(args[2]);//每次发消息操作发送几个

            //班级内某人的id为，班级id*100+人数（1~人数上线）

            MsgMain main = new MsgMain();

            for (int j = 70; j < 79; j++) {
                int b = j * 100;
                for (int m = b; m < b + 100; m++) {
                    int groupId = m;

                    int userId = groupId + 100;
                    Socket socket = main.startClient(userId, groupId, url);
                    sockets.put(userId, socket);
                    userIds.add(userId);
                }

                int c = j * 10000;
                for (int n = c; n < c + 100; n++) {
                    int groupId = n;

                    int userId = groupId + 100;
                    Socket socket = main.startClient(userId, groupId, url);
                    sockets.put(userId, socket);
                    userIds.add(userId);
                }
            }

            for (int j = 81; j < 82; j++) {
                int b = j * 100;
                for (int m = b; m < b + 100; m++) {
                    int groupId = m;

                    int userId = groupId + 100;
                    Socket socket = main.startClient(userId, groupId, url);
                    sockets.put(userId, socket);
                    userIds.add(userId);
                }

                int c = j * 10000;
                for (int n = c; n < c + 100; n++) {
                    int groupId = n;

                    int userId = groupId + 100;
                    Socket socket = main.startClient(userId, groupId, url);
                    sockets.put(userId, socket);
                    userIds.add(userId);
                }
            }

            Thread.sleep(10000);

            Timer timer = new Timer();
            SendTask sendTask = new SendTask(sendCountEveryTick);
            timer.scheduleAtFixedRate(sendTask, 0, sendTick);

        }
        else {
            System.out.println("参数错误");
        }

    }

    public static class SendTask extends TimerTask {
        private AtomicInteger count = new AtomicInteger(0);
        private int sendCountEveryTick;

        public SendTask(int sendCountEveryTick) {
            this.sendCountEveryTick = sendCountEveryTick;
        }

        @Override
        public void run() {
            int c = count.incrementAndGet();
            org.json.JSONObject json = new org.json.JSONObject();
            try {
                for (int i = 0; i < this.sendCountEveryTick; i++) {
                    int randomUserId = (int) (Math.random() * userIds.size());
                    Socket s = sockets.get(userIds.get(randomUserId));

                    json.put("type", 1);
                    json.put("content", randomUserId + "_发送的消息_" + c);
                    org.json.JSONObject target = new org.json.JSONObject();
                    target.put("id", 0);
                    target.put("type", 1);
                    json.put("target", target);
                    s.emit("msg", json.toString());
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket startClient(final int personId, final int groupId, final String url)
            throws URISyntaxException {

        IO.Options options = new IO.Options();
        options.transports = new String[] { "websocket" };
        options.forceNew = true;
        options.reconnectionDelay = 5000;//毫秒
        options.reconnectionAttempts = 30;//最多重试30次

        final Socket socket = IO.socket(url, options);

        final String userName = "USER_";

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("已连上=》groupId:" + groupId + " userId:" + personId);
                org.json.JSONObject json = new org.json.JSONObject();
                try {
                    json.put("id", personId);
                    json.put("name", userName + personId);
                    json.put("groupId", groupId);
                    json.put("token", "abc");
                    json.put("type", 0);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("auth", json.toString());
                System.out.println("emit auth");
            }
        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("重连=》groupId:" + groupId + " userId:" + personId + " :"
                        + args[0]);

            }
        }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("正在重连=》groupId:" + groupId + " userId:" + personId + " :"
                        + args[0]);

            }
        }).on("msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("收到msg=》groupId:" + groupId + " userId:" + personId + " :"
                        + args[0]);

            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println(personId + "连接断开");
            }
        });
        socket.connect();

        return socket;
    }

}
