package protocol;

import java.io.*;
import java.net.Socket;
import java.util.Vector;


//msg打包类
public class Protocol implements Serializable {

    public static final int MSG_TYPE_GAME_MSG_FROM_SERVER=0;
    public static final int MSG_TYPE_GAME_MSG_FROM_CLIENT=1;
    public static final int MSG_TYPE_LOGI_MSG = 2;
    public static final int MSG_TYPE_LOGI_UNITY_UPDATE_UI =3;

    private int msg_type;//true represent 现在是server的msg，false代表现在是client的msg



    private GameMsgFromServer _game_msgFromServer;
    private GameMsgFromClient _game_msgFromClient;
    private MsgLogic _msgLogic;
    private MsgUnity _msgUnity;

    public Protocol(int type){
        assert (type>=0&&type<4);
        msg_type=type;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public GameMsgFromServer get_game_msgFromServer() {
        return _game_msgFromServer;
    }

    public GameMsgFromClient get_game_msgFromClient() {
        return _game_msgFromClient;
    }

    public MsgLogic get_msgLogic() {
        return _msgLogic;
    }

    public MsgUnity get_msgUnity() {
        return _msgUnity;
    }


    public void set_game_msgFromServer(GameMsgFromServer mfs, int msgPass){
        _game_msgFromServer =mfs;

        if(_game_msgFromServer.getType()== GameMsgFromServer.NOW_YOUR_COLOR_HAVE_BEEN_INIT)//自身被初始化的颜色
        {
            _game_msgFromServer.setMyColor(msgPass);
            return ;
        }
        if(_game_msgFromServer.getType()== GameMsgFromServer.WHOSE_TURN)//谁的回合
        {
            _game_msgFromServer.setNowTurnColor(msgPass);
            return ;
        }

        if(_game_msgFromServer.getType()== GameMsgFromServer.REQUEST_DICE)//请求扔骰子                (playerId(nowTurnColor))
        {
            _game_msgFromServer.setNowTurnColor(msgPass);
            return ;
        }
    }
    public void set_game_msgFromServer(GameMsgFromServer mfs, Vector msgPass){
        _game_msgFromServer =mfs;

        if(_game_msgFromServer.getType()== GameMsgFromServer.LOGIC_SERIAL)//逻辑序列
        {
            _game_msgFromServer.setLogicSequence(msgPass);
            return ;
        }
    }

    public void set_game_msgFromServer(GameMsgFromServer mfs, Vector msgPass, int playerId){
        _game_msgFromServer =mfs;

        if(_game_msgFromServer.getType()== GameMsgFromServer.CHESS_TO_CHOOSE)////可选棋子
        {
            _game_msgFromServer.setSelectableChesses(msgPass,playerId);
            return ;
        }
    }

    public void set_game_msgFromClient(GameMsgFromClient mfc, int msgPass){//msgPass 仅用于传值
        _game_msgFromClient=mfc;
        if(_game_msgFromClient.getType()== GameMsgFromClient.DICE_NUM)//筛子数值
        {
            _game_msgFromClient.setDice(msgPass);
            return ;
        }
        if(_game_msgFromClient.getType()== GameMsgFromClient.THE_CHESS_WHO_CHOOSE)//选的棋子
        {
            _game_msgFromClient.setChosenChess(msgPass);
            return ;
        }
    }

    public void set_game_msgFromClient(GameMsgFromClient mfc){
        _game_msgFromClient=mfc;
    }


    public void set_msgLogic(MsgLogic ml){//无操作数
        _msgLogic=ml;
    }
    public void set_msgLogic(MsgLogic ml, int data){
        _msgLogic=ml;
        if(_msgLogic.getType() == MsgLogic.UPDATE_XP)
        {
            _msgLogic.setXP(data);
        }
    }
    public void set_msgLogic(MsgLogic ml, String msgPass){//msgpass用于传值
        _msgLogic=ml;
        if(_msgLogic.getType() == MsgLogic.REQUEST_ROOM_STATUS || _msgLogic.getType() == MsgLogic.REQUEST_XP)
        {
            _msgLogic.setRoomName(msgPass);
        }
    }
    public void set_msgLogic(MsgLogic ml, String str1,String str2)
    {
        _msgLogic = ml;
        if(_msgLogic.getType() == MsgLogic.REQUEST_CREATE_ROOM)
        {
            //房间名,房主名字
            _msgLogic.setRoomName(str1);
            _msgLogic.setUsr(str2);
        }
    }
    public void set_msgLogic(MsgLogic ml,Vector<String> vector)
    {
        _msgLogic = ml;
        if(_msgLogic.getType() == MsgLogic.UPDATE_ONLINE_ROOM)
        {
            _msgLogic.setOnlineRooms(vector);
        }
        else if(_msgLogic.getType() == MsgLogic.UPDATE_ROOM_STATUS)
        {
            _msgLogic.setRoomStatus(vector);
        }
    }


    public void set_msgUnity(MsgUnity mu){//无操作数
        _msgUnity=mu;
        if(_msgUnity.getType()== MsgUnity.S2C_JOIN_ROOM_SUCCESS||
                _msgUnity.getType()== MsgUnity.S2C_JOIN_ROOM_FAIL||
                _msgUnity.getType()== MsgUnity.S2C_INROOM_GAME_START||
                _msgUnity.getType()== MsgUnity.S2C_INROOM_GAME_FINISH){
            //无操作数直接忽视msgPass
            //server返回加入是否成功信息（是 和 否 两条枚举信息）（刷ui）       （无操作数）
            //server 在房间内 播报开始游戏指令（刷新ui）                             （无操作数）
            //server 在房间内 播报游戏结束（退出游戏ui）                                （无操作数）
            return;
        }
    }
    public void set_msgUnity(MsgUnity mu, String msgPass){//msgPass 仅用于传值
        _msgUnity=mu;
        if(_msgUnity.getType()== MsgUnity.S2C_INROOM_SB_QUIT_ROOM)//server 在房间内 播报某某某退出房间指令（刷新ui）             （personQuit）
        {
            _msgUnity.setPersonQuit(msgPass);
            return ;
        }
    }
    public void set_msgUnity(MsgUnity mu, Vector<String> msgVectorPass){//msgVectorPass 仅用于传值
        _msgUnity=mu;
        if(_msgUnity.getType()== MsgUnity.S2C_ROOM_LIST)//server向client发送当前已有房间号  当前房间列表（刷新ui）          (roomList)
        {
            _msgUnity.setRoomList(msgVectorPass);
            return ;
        }
        if(_msgUnity.getType()== MsgUnity.S2C_INROOM_ROOM_PERSONS)//client成功加入房间后，server向房间内所有人播报当前在房间里的person（刷新ui）  （roomPersons）
        {
            _msgUnity.setRoomPersons(msgVectorPass);
            return ;
        }
    }


    public void server_send2Client(){

    }

    public void server_recv(){}

    public void client_send2Server(){}

    public void client_recv(){}


    public String toString(){
        switch (msg_type){
            case Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER:
                return _game_msgFromServer.toString();
            case Protocol.MSG_TYPE_GAME_MSG_FROM_CLIENT:
                return _game_msgFromClient.toString();

            case Protocol.MSG_TYPE_LOGI_MSG:
                return _msgLogic.toString();
            case Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI:
                return _msgUnity.toString();
        }
        return "[ MsgNull ]";
    }

    //序列化后直接传送给socket对象
    public static void socketSerilize(Socket socket, Protocol msg){
        try {
            OutputStream outputStream  = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(msg);

            objectOutputStream.flush();
            //objectOutputStream.close(); 会把socket关掉
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Protocol socketUnSerilize(Socket socket){
        Protocol rb=null;
        try {

            InputStream inputStream = socket.getInputStream();
            //BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //if (is.ready()) {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            rb = (Protocol) objectInputStream.readObject();
            return rb;

            // }
            //else return null;
        }
        catch (ClassNotFoundException e) {
            System.out.println("err 收到客户端的请求不能发序列化到Protocal");
            //e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return rb;
    }
    public static void main(String[] args) {

        Vector<Protocol> testExamples=new Vector<>();
        Protocol tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER);
        tmp1.set_game_msgFromServer(new GameMsgFromServer(GameMsgFromServer.NOW_YOUR_COLOR_HAVE_BEEN_INIT),2);
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER);
        tmp1.set_game_msgFromServer(new GameMsgFromServer(GameMsgFromServer.WHOSE_TURN),3);
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER);
        Vector<Integer> selectableChess=new Vector<>();
        selectableChess.add(new Integer(0));
        selectableChess.add(new Integer(1));
        selectableChess.add(new Integer(3));
        tmp1.set_game_msgFromServer(new GameMsgFromServer(GameMsgFromServer.CHESS_TO_CHOOSE),selectableChess,2);
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER);
        tmp1.set_game_msgFromServer(new GameMsgFromServer(GameMsgFromServer.REQUEST_DICE),3);//msgPass 为 playerId
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_SERVER);
        Vector<String > logicSeri=new Vector<>();
        logicSeri.add("不哄我");
        logicSeri.add("是吗");
        logicSeri.add("那就不红吧");
        tmp1.set_game_msgFromServer(new GameMsgFromServer(GameMsgFromServer.LOGIC_SERIAL),logicSeri);
        testExamples.add(tmp1);

        //---------------
        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_CLIENT);
        tmp1.set_game_msgFromClient(new GameMsgFromClient(GameMsgFromClient.DICE_NUM),6);
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_GAME_MSG_FROM_CLIENT);
        tmp1.set_game_msgFromClient(new GameMsgFromClient(GameMsgFromClient.THE_CHESS_WHO_CHOOSE),2);
        testExamples.add(tmp1);

        //-----------------
        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        //set room list
        Vector<String > roomlist=new Vector<>();
        roomlist.add("room:不哄我");
        roomlist.add("room:是吗");
        roomlist.add("room:那就不红吧");
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_ROOM_LIST),roomlist);
        testExamples.add(tmp1);


        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_JOIN_ROOM_SUCCESS));
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_JOIN_ROOM_FAIL));
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        //set room person
        Vector<String > roomPerson=new Vector<>();
        roomPerson.add("Cmhowl");
        roomPerson.add("AJJDF");
        roomPerson.add("CJJ");
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_INROOM_ROOM_PERSONS),roomPerson);
        testExamples.add(tmp1);


        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_INROOM_GAME_START));
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_INROOM_SB_QUIT_ROOM),"cmhowl");
        testExamples.add(tmp1);

        tmp1=new Protocol(Protocol.MSG_TYPE_LOGI_UNITY_UPDATE_UI);
        tmp1.set_msgUnity(new MsgUnity(MsgUnity.S2C_INROOM_GAME_FINISH));
        testExamples.add(tmp1);

        System.out.println(testExamples);


    }
}
