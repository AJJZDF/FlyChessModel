package protocol;

import java.io.Serializable;
import java.util.Vector;

public class MsgLogic implements Serializable {
    //add...
    public static final int REQUEST_ONLINE_ROOM = 1;                //请求刷新房间列表     ()
    public static final int REQUEST_CREATE_ROOM = 2;//请求创建房间
    public static final int UPDATE_ONLINE_ROOM = 3; // 向客户端返回在线房间列表
    public static final int REQUEST_ROOM_STATUS = 4;//请求更新房间状态
    public static final int UPDATE_ROOM_STATUS = 5;//向客户端返回新的房间状态
    public static final int REQUEST_XP = 6;//请求获得自己的p数
    public static final int UPDATE_XP = 7;//请求刷新自己的p数

    private int type;
    //---------------数据
    private int num;
    private String roomname=null;
    private String usr=null;
    private String tryCreateRoomName=null;
    private Vector<String > vector;

    public MsgLogic(int _type){
        type=_type;
    }

    public void setRoomName(String roomname){this.roomname = roomname;}
    public String getRoomName(){return roomname;}

    public void setUsr(String usr) {
        this.usr = usr;
    }
    public String getUsr() {
        return usr;
    }

    public void setTryCreateRoomName(String tryCreateRoomName) {
        this.tryCreateRoomName = tryCreateRoomName;
    }
    public String getTryCreateRoomName() {
        return tryCreateRoomName;
    }

    public void setOnlineRooms(Vector<String> onlineRooms)
    {
        this.vector = onlineRooms;
    }
    public Vector<String> getOnlineRooms(){ return vector;}


    public void setRoomStatus(Vector<String> roomStatus)
    {
        this.vector = roomStatus;
    }

    public Vector<String> getRoomStatus()
    {
        return vector;
    }

    public void setXP(int xp)
    {
        this.num = xp;
    }
    public int getXP(){ return num; }

    @Override
    public String toString() {

        if(this.type == MsgLogic.REQUEST_ONLINE_ROOM)
        {
            return "[ "+ "REQUEST_ONLINE_ROOM" + " ]";
        }

        if(this.type == MsgLogic.REQUEST_CREATE_ROOM)
        {
            return "[ "+ "REQUEST_CREATE_ROOM" + " ]";
        }
        if(this.type == MsgLogic.REQUEST_ROOM_STATUS)
        {
            return "[ "+ "REQUEST_ROOM_STATUS" + " ]";
        }
        if(this.type == MsgLogic.REQUEST_XP)
        {
            return "[ "+ "REQUEST_XP" + " ]";
        }

        if(this.type== MsgLogic.UPDATE_ONLINE_ROOM)//逻辑序列                   (online_rooms)
            return "[ MsgType = UPDATE_ONLINE_ROOM    "
                    +"Intent = LOGIC_SERIAL    "
                    +"在线房间    "
                    +" online_rooms = "
                    + vector.toString()
                    +" ]" ;

        if(this.type== MsgLogic.UPDATE_ROOM_STATUS)//逻辑序列                   (online_rooms)
            return "[ MsgType = UPDATE_ROOM_STATUS    "
                    +"Intent = LOGIC_SERIAL    "
                    +"状态房间    "
                    +" room_status = "
                    + vector.toString()
                    +" ]" ;

        return "[error type]";
    }
    public int getType(){
        return type;
    }
}