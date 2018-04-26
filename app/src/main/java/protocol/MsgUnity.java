package protocol;

import java.io.Serializable;
import java.util.Vector;

/*
房间流程            (带inroom都是房间内指令)
server向client发送当前已有房间号  当前房间列表（刷新ui）  S2C_ROOM_LIST
client发送试图进入哪个房间                                C2S_CLIENT_TRY_JOIN_ROOM
server返回加入是否成功信息(是 和 否 两条枚举信息)（刷ui） S2C_JOIN_ROOM_SUCCESS/S2C_JOIN_ROOM_FAIL
    client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）    S2C_INROOM_ROOM_PERSONS
    client发送退出房间指令                                                          C2S_INROOM_QUIT_ROOM
    server播报某某某退出房间指令（刷新ui）                                          S2C_INROOM_SB_QUIT_ROOM
房主发送，申请开始游戏指令。                              C2S_INROOM_TRY_GAME_START
server播报开始游戏指令（刷新ui）                          S2C_INROOM_GAME_START

server播报游戏结束（退出游戏ui）                          S2C_INROOM_GAME_FINISH



创建房间：
client 发送房间名string 给server                                      protocol SEND_CREATE_ROOM
server（map《string roomname，room 》 判断是否有这个房间key）
如果 没有，就返回创建成功，以及房间信息（room对象）。                   protocol
如果有，返回创建失败                                                      protocol

 */

public class MsgUnity implements Serializable{//用于unity 更新ui


    public static final int S2C_ROOM_LIST=0;                //server向client发送当前已有房间号  当前房间列表（刷新ui）          (roomList)

    public static final int S2C_JOIN_ROOM_SUCCESS=1;        //server返回加入是否成功信息（是 和 否 两条枚举信息）（刷ui）       （无操作数）
    public static final int S2C_JOIN_ROOM_FAIL=2;

    public static final int S2C_INROOM_ROOM_PERSONS=3;      //client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）  （roomPersons）

    public static final int S2C_INROOM_GAME_START=4;        //server 在房间内 播报开始游戏指令（刷新ui）                             （无操作数）

    public static final int S2C_INROOM_SB_QUIT_ROOM=5;      //server 在房间内 播报某某某退出房间指令（刷新ui）                          （personQuit）

    public static final int S2C_INROOM_GAME_FINISH=6;       //server 在房间内 播报游戏结束（退出游戏ui）                                （无操作数）


    private int type;
    //------------数据
        //server向client发送当前已有房间号  当前房间列表（刷新ui）                  (roomList)
    private Vector<String> roomList=new Vector<String>();
        //client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）  （roomPersons）
    private Vector<String> roomPersons=new Vector<String>();
    private String personQuit=null;


    public MsgUnity(int _type){
        type=_type;
    }
    public void setRoomList(Vector<String> roomList) {
        this.roomList = roomList;
    }

    public void setRoomPersons(Vector<String> roomPersons) {
        this.roomPersons = roomPersons;
    }

    public void setPersonQuit(String personQuit) {
        this.personQuit = personQuit;
    }


    @Override
    public String toString() {
        if(this.type== MsgUnity.S2C_ROOM_LIST)//server向client发送当前已有房间号  当前房间列表（刷新ui）          (roomList)
            return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                    +"Intent = S2C_ROOM_LIST    "
                    +"server向client发送当前已有房间号  当前房间列表（刷新ui）\n"
                    +" roomList = "
                    +this.roomList
                    +" ]";

        if(this.type== MsgUnity.S2C_INROOM_ROOM_PERSONS)//client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）  （roomPersons）
            return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                    +"Intent = S2C_INROOM_ROOM_PERSONS \n"
                    + "client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）\n"
                    +" roomPersons = "
                    +this.roomPersons
                    +" ]";

        if(this.type== MsgUnity.S2C_INROOM_SB_QUIT_ROOM)//server 在房间内 播报某某某退出房间指令（刷新ui）             （personQuit）
            return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                    +"Intent = S2C_INROOM_SB_QUIT_ROOM    "
                    + "server 在房间内 播报某某某退出房间指令（刷新ui）    "
                    +" personQuit = "
                    +this.personQuit
                    +" ]";

        if(this.type== MsgUnity.S2C_JOIN_ROOM_SUCCESS||
                this.type== MsgUnity.S2C_JOIN_ROOM_FAIL||
                this.type== MsgUnity.S2C_INROOM_GAME_START||
                this.type== MsgUnity.S2C_INROOM_GAME_FINISH) {
            //server返回加入是否成功信息（是 和 否 两条枚举信息）（刷ui）       （无操作数）
            //server 在房间内 播报开始游戏指令（刷新ui）                             （无操作数）
            //server 在房间内 播报游戏结束（退出游戏ui）                                （无操作数）

            switch (type) {
                case MsgUnity.S2C_JOIN_ROOM_SUCCESS:
                    return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                            + "Intent = S2C_JOIN_ROOM_SUCCESS"
                            + " ]";
                case MsgUnity.S2C_JOIN_ROOM_FAIL:
                    return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                            + "Intent = S2C_JOIN_ROOM_FAIL"
                            + " ]";
                case MsgUnity.S2C_INROOM_GAME_START:
                    return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                            + "Intent = S2C_INROOM_GAME_START"
                            + " ]";
                case MsgUnity.S2C_INROOM_GAME_FINISH:
                    return "[ MsgType = LOGI_UNITY_UPDATE_UI    "
                            + "Intent = S2C_INROOM_GAME_FINISH"
                            + " ]";
            }
        }
        return "[error type]"    ;
    }

    public int getType(){
        return type;
    }

    public String getPersonQuit() {
        return personQuit;
    }

    public Vector<String> getRoomList() {
        return roomList;
    }

    public Vector<String> getRoomPersons() {
        return roomPersons;
    }

}

