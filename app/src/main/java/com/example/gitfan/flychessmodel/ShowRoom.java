package com.example.gitfan.flychessmodel;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;
import protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ShowRoom extends AppCompatActivity  implements View.OnClickListener{

    ListView showRoom;//用来显示在线房间的

    Button updateRoom;//刷新按钮

    private static final String TAG = "fucking_msg";//调试

    private String [] online_rooms = {};//在线房间列表

    //消息类型,必须是static final类型
    private static final int UPDATE_ONLINE_ROOM = 1;//刷新在线房间
    private static final int UPDATE_ROOM_STATUS = 2;//更新房间状态
    private static final int UPDATE_XP = 3;//更新p书

    //使用消息机制来实现在子进程中更新主线程的UI
    private Handler handler = new Handler() {
        public void handleMessage(Message message)
        {
            //记得每个分支都要break
            switch (message.what)
            {
                //刷新在线房间
                case UPDATE_ONLINE_ROOM:
                {
                    Bundle bundle = message.getData();
                    //形式:roomname_roomowner_1/4
                    Vector<String> rooms = (Vector<String>) bundle.getSerializable("online_rooms");

                    online_rooms = new String[rooms.size()];
                    for(int i = 0; i < rooms.size(); i++)
                    {
                        online_rooms[i] = rooms.get(i);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowRoom.this,android.R.layout.simple_list_item_1,online_rooms);
                    adapter.notifyDataSetChanged();
                    showRoom.setAdapter(adapter);
                    String str = "size: " + rooms.size();
                    for(String name:online_rooms){
                        str = str + " " + name;
                    }
                    Log.d(TAG,str);
                    break;
                }
                //刷新房间状态
                case UPDATE_ROOM_STATUS:
                {
                    Bundle bundle = message.getData();
                    Vector<String> rooms = (Vector<String>) bundle.getSerializable("room_status");
                    Log.d(TAG,rooms.toString());
                    break;
                }
                //更新p数
                case UPDATE_XP:
                {
                    Bundle bundle = message.getData();
                    int xp = bundle.getInt("update_xp");
                    Log.d(TAG,"get xp: " + xp);
                    break;
                }
            }
        }
    };

    //后台线程:不断接收信息
    DameoThread recvThread;

    class DameoThread extends Thread
    {
        Socket read_socket;
        public DameoThread(){

        }
        //使用消息机制刷新UI
        Message message;
        Bundle data;

        private void initMsg(int msg_kind)
        {
            message = new Message();
            message.what = msg_kind;
            data = new Bundle();
        }
        private void putInt(String key,int val)
        {
            data.putInt(key,val);
        }
        private void putString(String key,String val)
        {
            data.putString(key,val);
        }
        private void putVector(String key,Vector<String> vector)
        {
            data.putSerializable(key,vector);
        }
        private void send_msg()
        {
            message.setData(data);
            handler.sendMessage(message);
        }

        //解析从服务器接发送过来的信息,根据消息类型刷新UI
        private void process_msg(Protocol protocol)
        {
            //消息类型
            switch (protocol.getMsg_type())
            {
                case Protocol.MSG_TYPE_LOGI_MSG:
                {
                    //获取具体消息
                    MsgLogic msgLogic = protocol.get_msgLogic();

                    //再细分消息类型
                    switch (msgLogic.getType())
                    {
                        //服务器发过来的刷新房间消息的消息
                        case MsgLogic.UPDATE_ONLINE_ROOM:
                        {
                            //获取新的房间列表
                            Vector<String> _online_rooms = msgLogic.getOnlineRooms();

                            //将新的房间列表_online_rooms放入message,然后通过handle发送message更新UI
                            initMsg(UPDATE_ONLINE_ROOM);
                            putVector("online_rooms",_online_rooms);
                            send_msg();
                            break;
                        }
                        //更新房间状态
                        case MsgLogic.UPDATE_ROOM_STATUS:
                        {
                            //获取新的房间状态
                            Vector<String> room_status = msgLogic.getRoomStatus();

                            //将新的房间状态room_status放入message,然后通过handle发送message更新UI
                            initMsg(UPDATE_ROOM_STATUS);
                            putVector("room_status",room_status);
                            send_msg();
                            break;
                        }
                        //更新p数
                        case MsgLogic.UPDATE_XP:
                        {
                            //获得p数
                            int xp = msgLogic.getXP();

                            //将p数发送到handler
                            initMsg(UPDATE_XP);
                            putInt("update_xp",xp);
                            send_msg();
                            break;
                        }
                        default:
                            break;
                    }

                    break;
                }
                default:
                    break;
            }
        }

        private void start_thread() throws IOException {

            read_socket = new Socket(UnifiedStandard.SERVER_ADDR,UnifiedStandard.SERVER_WRITE_PORT);

            //不断接收信息
            while(true)
            {
                //获取得到的Protocol
                Protocol recvMsg = Protocol.socketUnSerilize(read_socket);
                if(recvMsg == null)
                {
                    Log.d(TAG, "fucking object null");
                    continue;
                }
                Log.d(TAG, "recv_msg: " + recvMsg.toString());

                process_msg(recvMsg);//处理接收到的信息

                Log.d(TAG, "finish ");
            }
        }

        @Override
        public void run() {

            try {
                start_thread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //发送请求刷新房间的信息给服务器
    private void send_updateroom_request_to_server() throws IOException {

        Protocol msg_send = new Protocol(Protocol.MSG_TYPE_LOGI_MSG);

        msg_send.set_msgLogic(new MsgLogic(MsgLogic.REQUEST_ONLINE_ROOM));

        //连接服务器,发送请求
        Socket socket = new Socket(UnifiedStandard.SERVER_ADDR,UnifiedStandard.SERVER_READ_PORT);

        //protocol对象序列化传送
        Protocol.socketSerilize(socket,msg_send);

        socket.close();
    }
    //创建房间
    private void send_createroom_request_to_server(String roomname,String roomowner) throws IOException {

        Protocol msg_send = new Protocol(Protocol.MSG_TYPE_LOGI_MSG);

        msg_send.set_msgLogic(new MsgLogic(MsgLogic.REQUEST_CREATE_ROOM),roomname,roomowner);

        //连接服务器,发送请求
        Socket socket = new Socket(UnifiedStandard.SERVER_ADDR,UnifiedStandard.SERVER_READ_PORT);

        //protocol对象序列化传送
        Protocol.socketSerilize(socket,msg_send);

        socket.close();
    }

    //请求刷新房间状态
    private void send_update_roomstatus_to_server(String roomname) throws IOException {

        Protocol msg_send = new Protocol(Protocol.MSG_TYPE_LOGI_MSG);

        msg_send.set_msgLogic(new MsgLogic(MsgLogic.REQUEST_ROOM_STATUS),roomname);

        //连接服务器,发送请求
        Socket socket = new Socket(UnifiedStandard.SERVER_ADDR,UnifiedStandard.SERVER_READ_PORT);

        //protocol对象序列化传送
        Protocol.socketSerilize(socket,msg_send);

        socket.close();
    }

    private void send_getxp_to_server(String roomname)throws IOException
    {
        Protocol msg_send = new Protocol(Protocol.MSG_TYPE_LOGI_MSG);

        msg_send.set_msgLogic(new MsgLogic(MsgLogic.REQUEST_XP),roomname);

        //连接服务器,发送请求
        Socket socket = new Socket(UnifiedStandard.SERVER_ADDR,UnifiedStandard.SERVER_READ_PORT);

        //protocol对象序列化传送
        Protocol.socketSerilize(socket,msg_send);

        socket.close();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            //刷新房间
            case R.id.updateRoom:
            {
                //注意:一个APP如果在主线程中请求网络操作，将会抛出此异常
                //所以要new一个子线程去进行socket操作
                new Thread()
                {
                    @Override
                    public void run() {
                        try {

                            send_createroom_request_to_server("room1","gitfan");
                            Thread.sleep(1000);
                            send_updateroom_request_to_server();
                            Thread.sleep(1000);
                            send_update_roomstatus_to_server("room1");
                            Thread.sleep(1000);
                            send_getxp_to_server("room1");

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "run: " + e.toString());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_room);

        showRoom = (ListView) findViewById(R.id.showroom);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowRoom.this,android.R.layout.simple_list_item_1,online_rooms);
        showRoom.setAdapter(adapter);

        updateRoom = (Button)findViewById(R.id.updateRoom);

        updateRoom.setOnClickListener(this);

        //运行后台线程(不终止)
        recvThread = new DameoThread();
        recvThread.start();

    }
}
