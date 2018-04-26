package protocol;

import java.io.Serializable;
import java.util.Vector;

//server
//send2Client  谁的回合
//             可选棋子
//             逻辑序列
public class GameMsgFromServer implements Serializable {
    public final static int NOW_YOUR_COLOR_HAVE_BEEN_INIT=0;           //自身被初始化的颜色       (myColor)
    public final static int WHOSE_TURN=1;                              //谁的回合                 (nowTurnColor)
    public final static int CHESS_TO_CHOOSE=2;                         //可选棋子                  (selectableChesses，playerId(nowTurnColor))轮到自己选棋子
    public final static int LOGIC_SERIAL=3;                            //逻辑序列                   (LogicSequence)

    public final static int REQUEST_DICE=4;                             //请求扔骰子                (playerId(nowTurnColor))

    private int type;

    //-------数据
    private int myColor;
    private int nowTurnColor;
    private Vector<Integer> selectableChesses;
    private Vector<String > LogicSequence;

    public GameMsgFromServer(int _type){
        type=_type;
    }
    public void setMyColor(int i){this.myColor=i;}

    public void setNowTurnColor(int nowTurnColor) {
        this.nowTurnColor = nowTurnColor;
    }

    public void setSelectableChesses(Vector<Integer> selectableChesses,int playerId) {
        this.selectableChesses = selectableChesses;
        this.nowTurnColor=playerId;
    }

    public void setLogicSequence(Vector<String> logicSequence) {
        LogicSequence = logicSequence;
    }

    public String  toString(){
        if(this.type== GameMsgFromServer.NOW_YOUR_COLOR_HAVE_BEEN_INIT)//自身被初始化的颜色
            return "[ MsgType = GAME_MSG_FROM_SERVER    "
                    +"Intent = "
                    + "NOW_YOUR_COLOR_HAVE_BEEN_INIT    "
                    +"自身被初始化的颜色    "
                    +" myColor = "
                    +myColor
                    +" ]" ;
        if(this.type== GameMsgFromServer.WHOSE_TURN)//谁的回合                 (nowTurnColor)
            return "[ MsgType = GAME_MSG_FROM_SERVER    "
                    +"Intent = WHOSE_TURN    "
                    +"谁的回合    "
                    +" nowTurnColor = "
                    + nowTurnColor
                    +" ]" ;
        if(this.type== GameMsgFromServer.CHESS_TO_CHOOSE)//可选棋子                  (selectableChesses)
            return "[ MsgType = GAME_MSG_FROM_SERVER    "
                    +"Intent = CHESS_TO_CHOOSE    "
                    +"playerId(nowTurnColor) = "
                    +nowTurnColor
                    +"  可选棋子    "
                    +" selectableChesses = "
                    +selectableChesses
                    +" ]" ;
        if(this.type== GameMsgFromServer.LOGIC_SERIAL)//逻辑序列                   (LogicSequence)
            return "[ MsgType = GAME_MSG_FROM_SERVER    "
                    +"Intent = LOGIC_SERIAL    "
                    +"在线的房间    "
                    +" LogicSequence = "
                    +LogicSequence

                    +" ]" ;


        if(this.type== GameMsgFromServer.REQUEST_DICE)//请求扔骰子                   (playerId(nowTurnColor))
            return "[ MsgType = GAME_MSG_FROM_SERVER    "
                    +"Intent = REQUEST_DICE    "
                    +"请求扔骰子    "
                    +" playerId(nowTurnColor) = "
                    +nowTurnColor
                    +" ]" ;
        return "[error type]";
    }

    public int getType(){
        return type;
    }

    public int getMyColor() {
        return myColor;
    }

    public int getNowTurnColor() {
        return nowTurnColor;
    }

    public Vector<Integer> getSelectableChesses() {
        return selectableChesses;
    }

    public Vector<String> getLogicSequence() {
        return LogicSequence;
    }

}