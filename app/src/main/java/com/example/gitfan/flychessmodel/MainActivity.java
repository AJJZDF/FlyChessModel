package com.example.gitfan.flychessmodel;

import FlyChess.*;


import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //消息类型,必须是static final类型
    private static final int TURN = 1;//设置现在轮到谁
    private static final int REQUESTDICE = 2;//请求扔骰子
    private static final int THROWDICE = 3;//真正扔骰子，播放扔骰子的动画
    private static final int SHOW_THROWDICE_BUTTON = 4;//显示扔骰子的那个按钮
    private static final int SET_AVAILABLE_CHESS = 5;//设置可用的棋子
    private static final int SHOW_ACTION_LIST = 6;//用unity翻译动作指令集
    private static final int SELECT_CHESS = 7;//AI选择棋子


    private void set_available_chess(String allChoices)
    {
        String choicelist [] = allChoices.split("\\s+");//以空格分割字符串，得到每一个选择

        //如果没有棋子可供选择
        if(choicelist.length == 0)
        {
            //提示一下玩家：没有任何选择，同时，设置所有的棋子为不可选
            brokenAll();
            Toast.makeText(MainActivity.this,"No choice",Toast.LENGTH_SHORT).show();

            //注意：没有棋子选择时要设置为-1
            dameoThread.gameManager.setChoice(-1);//很重要，要设置为-1
        }
        else
        {
            //设置相应的棋子为可用，其他棋子不可用
            brokenAll();//...
            int playerid = dameoThread.gameManager.getTurn();//ps：通过getTurn来获得当前的玩家是谁，然后去设置对应玩家的棋子为可用
            for(String choose:choicelist)
            {
                //为了去掉可能存在的空白字符
                if(choose.length() != 0)
                {
                    int choice =  Integer.parseInt(choose);//转化为数字
                    canClicked[playerid*4 + choice]  = true;//通过设置canClicked数组来真正使得棋子是否可用
                    chesslist[playerid][choice].setBackgroundColor(buttonColor(playerid));//形式上把可用的棋子设置为相应的玩家颜色来表示可用
                }
            }

            //然后就可以等用户自己选择棋子了
            //选了棋子之后会做什么？？
            //其实记得最后调用gameManager.setChoice(choice)，就OK的
        }
    }


    //使用消息机制来实现在子进程中更新主线程的UI
    private Handler handler = new Handler() {

        public void handleMessage(Message message)
        {
            //记得每个分支都要break
            switch (message.what)
            {
                //现在轮到谁了
                case TURN:
                {
                    int turn = message.getData().getInt("turn");
                    turnView.setText(NOW + colorToString(turn) + PLAYER);
                    showDice.setVisibility(View.INVISIBLE);
                    //showActionList.setVisibility(View.INVISIBLE);
                    break;
                }
                //请求扔一个骰子
                case REQUESTDICE:
                {
                    brokenAll();
                    break;
                }
                //播放扔骰子的动画
                case THROWDICE:
                {
                    int dice = message.getData().getInt("dice");//获取骰子数

                    //然后下面根据点数播放扔骰子的动画

                    /****************************************/

                    //这里简单模拟一下这个过程

                    //Toast.makeText(MainActivity.this,"now,throwing a dice",Toast.LENGTH_SHORT).show();
                    showDice.setVisibility(View.VISIBLE);//先设置为可见
                    showDice.setText(diceToString(dice));
                    //showActionList.setVisibility(View.INVISIBLE);

                    /*****************************************/

                    //最后还是要调用gameManager.setDice(dice)
                    dameoThread.gameManager.setDice(dice);

                    break;
                }
                case SHOW_THROWDICE_BUTTON:
                {
                    //显示扔骰子的按钮
                    throwDice.setVisibility(View.VISIBLE);
                    break;
                }
                case SET_AVAILABLE_CHESS:
                {
                    String allChoices = message.getData().getString("availablechess");
                    //设置这些棋子为可用
                    set_available_chess(allChoices);
                    break;
                }
                case SHOW_ACTION_LIST:
                {
                    String actionlist = message.getData().getString("actionlist");
                    showActionList.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    showActionList.setVisibility(View.VISIBLE);
                    showActionList.setText(actionlist);
                    showActionList.setSingleLine(false);
                    showActionList.setHorizontallyScrolling(false);
                    //showDice.setVisibility(View.INVISIBLE);
                    break;
                }
                case SELECT_CHESS:
                {
                    int playerid = dameoThread.gameManager.getTurn();
                    int chess = message.getData().getInt("chess");
                    brokenAll();
                    if(chess != -1)
                    {
                        chesslist[playerid][chess].setBackgroundColor(buttonColor(playerid));
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"no choice",Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

            }
        }
    };

    //后台进程，控制着整个游戏
    class DameoThread extends Thread
    {
        public GameManager gameManager;

        public DameoThread(GameManager manager){
            gameManager = manager;
        }

        @Override
        public void run() {

            while(!gameManager.isGameOver())
            {
                //先去更新UI界面：告诉玩家现在轮到谁了
                Message message = new Message();
                message.what = TURN;
                Bundle data = new Bundle();
                data.putInt("turn",gameManager.getTurn()); //通过gameManager.getTurn()，可以知道现在轮到谁
                message.setData(data);
                handler.sendMessage(message);

                //然后去请求扔一个骰子，仅仅是请求，不是真的扔（毕竟当现在的玩家是人的时候，什么时候扔骰子还真不知道)
                requestDice();


                //请求扔骰子后，不断扫描是不是已经扔了骰子
                //只有真的扔了才可以进行下一步
                while(gameManager.waitDice()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /**************************************************/
                //对于取消托管很重要的一件事，AI的睡眠必须放在这里
                //因为有时间差，睡眠放在requestDice里面会出问题的
                //原因自己思考...

                if(gameManager.isAI())
                {
                    //睡眠一下，免得AI的动作太快...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /**************************************************/

                //进入一个新阶段：请求玩家去选定棋子
                requestChess();

                //不断扫描是不是有人选择了棋子
                while (gameManager.waitChoice()) try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                /**************************************************/
                //对于取消托管很重要的一件事，AI的睡眠必须放在这里
                //因为有时间差，睡眠放在requestDice里面会出问题的
                //原因自己思考...

                if(gameManager.isAI())
                {
                    //睡眠一下，免得AI的动作太快...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /**************************************************/

                //进入一个新的阶段：可以得到一系列动作指令集
                //   ~\(≧▽≦)/~啦啦啦

                //具体要以什么形式去翻译给unity，我就不清楚了
                //反正可以通过迭代器遍历出每个指令
                String actionlist = "";
                for(Action action:gameManager.actionlist())
                {
                    actionlist += action.toString() + "\n";
                }

                message = new Message();
                message.what = SHOW_ACTION_LIST;
                data = new Bundle();
                data.putString("actionlist",actionlist);
                message.setData(data);
                handler.sendMessage(message);

            }
        }
        //随机产生骰子点数
        public int newDice(){
            return ((int)(Math.random()*1000000))%6 + 1;
        }

        //请求投掷一个骰子
        //先通过gamemanager判断是人还是AI
        //如果是人，则应该出现投掷的按钮，并让玩家自己选择
        private void requestDice()
        {
            //请求骰子时：
            //step 1:设置所有的棋子不能被选择
            //steo 2:不显示扔骰子的按钮（ps：因为只有玩家才需要扔骰子，AI不需要显示扔骰子的按钮，这里统一设置为隐藏，后面有分支)
            Message message = new Message();
            message.what = REQUESTDICE;
            handler.sendMessage(message);

            //如果是AI
            if(gameManager.isAI())
            {
                //AI会主动扔骰子
                int dice = newDice();//随机产生一个dice

                //根据骰子数目来播放动画
                message = new Message();
                message.what = THROWDICE;
                Bundle data = new Bundle();
                data.putInt("dice",dice);
                message.setData(data);
                handler.sendMessage(message);


//                //睡眠一下，免得AI的动作太快...
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//

                //扔完骰子记得给gameManager设置骰子！！！
                //切记，很重要！！！
                gameManager.setDice(dice);   //ps:这句放到最后比较好一点...
            }
            //如果是人
            else
            {
                //如果是人，so easy，只需要显示那个扔骰子的按钮，让人主动去扔就完事了呀
                message = new Message();
                message.what = SHOW_THROWDICE_BUTTON;
                handler.sendMessage(message);

                //至于玩家点了扔骰子的按钮后要做什么呢？
                //还是那句话：扔完骰子记得给gameManager设置骰子！！！切记，很重要！！！
                //如果不设置的话，整个gameManager就卡死住了...
            }
        }

        //扔完骰子之后请求玩家（或者AI）选择一个棋子
        //玩家可以自己选棋子，而AI只能自动选棋子
        private void requestChess()
        {
            //如果是AI
            if(gameManager.isAI())
            {
                //如果是AI，则让AI自己选择
                int choose = gameManager.getAIChoice();

                Message message = new Message();
                message.what = SELECT_CHESS;
                Bundle data = new Bundle();
                data.putInt("chess",choose);
                message.setData(data);
                handler.sendMessage(message);


//                //睡眠一下，免得AI的动作太快...
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                //记得给gameManager设置棋子的选择！！！
                //切记，很重要！！！
                gameManager.setChoice(choose);
            }
            //如果是人，则让人自己选择哪个棋子
            else
            {

                //step 1: 获取可供选择的棋子（只是获得棋子编号：0,1,2,3）
                Queue<Integer> availableChoices = gameManager.getChessAvailable();

                //step 2: 设置这些棋子为可以被选择，其他的所有棋子都不能被选择

                //有一种情况是由于点数太小，没有可以移动的棋子
                //这时要提示玩家：没有棋子可以选择！！！
                //同时，一定要设置选择为-1！！！！
                //通过isEmpty来判断是不是没有选择

                String allChoices = "  ";//要初始化为空格字符，不能初始化为空白字符
                Message message = new Message();
                message.what = SET_AVAILABLE_CHESS;

                if(!availableChoices.isEmpty())
                {
                    for(Integer choose:availableChoices)
                    {
                        allChoices += choose + " ";//用空格分开每一个选择
                    }
                }

                Bundle data = new Bundle();
                data.putString("availablechess",allChoices);
                message.setData(data);
                handler.sendMessage(message);
            }
        }

    }
    DameoThread dameoThread;


    private Button chesslist[][];//16个棋子
    private EditText turnView;//现在轮到谁
    private Button throwDice;//投掷骰子
    private EditText showDice;//显示骰子
    private EditText showActionList; //显示动作列表
    private Button switchMode;//模式切换：AI -> 玩家 或 玩家 -> AI

    private boolean canClicked[];//是否可以点击，选择棋子时有用到

    //根据棋子的ID，返回选择le哪个棋子
    private int chessToChoice(int viewer_id,int playerID)
    {
        if(viewer_id == R.id.blue0 || viewer_id == R.id.green0
                || viewer_id == R.id.red0 || viewer_id == R.id.yellow0){
            if(playerID == dameoThread.gameManager.getTurn())
            {
                return 0;
            }
            else return -1;
        }
        else if(viewer_id == R.id.blue1 || viewer_id == R.id.green1
                || viewer_id == R.id.red1 || viewer_id == R.id.yellow1){
            if(playerID == dameoThread.gameManager.getTurn())
            {
                return 1;
            }
            else return -1;
        }
        else if(viewer_id == R.id.blue2 || viewer_id == R.id.green2
                || viewer_id == R.id.red2 || viewer_id == R.id.yellow2){

            if(playerID == dameoThread.gameManager.getTurn())
            {
                return 2;
            }
            else return -1;
        }
        else
        {
            if(playerID == dameoThread.gameManager.getTurn())
            {
                return 3;
            }
            else return -1;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.thrownDice:
            {
                int dice = dameoThread.newDice();//随机产生骰子

                //隐藏扔骰子的按钮（它在这轮游戏的使命完成了）
                throwDice.setVisibility(View.INVISIBLE);

                //然后下面根据点数播放扔骰子的动画

                /****************************************/

                //这里简单模拟一下这个过程

                //Toast.makeText(MainActivity.this,"now,throwing a dice",Toast.LENGTH_SHORT).show();
                showDice.setVisibility(View.VISIBLE);//先设置为可见
                showDice.setText(diceToString(dice));
                showActionList.setVisibility(View.INVISIBLE);


                /*****************************************/

                //扔完骰子记得给gameManager设置骰子！！！
                //切记，很重要！！！
                dameoThread.gameManager.setDice(dice);
                break;
            }
            case R.id.red0:case R.id.red1:case R.id.red2:case R.id.red3:
            {
                int playerid = dameoThread.gameManager.getTurn();
                int choice = chessToChoice(view.getId(),Chess.RED);

                if(choice != -1)
                {
                    if(canClicked[playerid*4 + choice])
                    {
                        brokenAll();//防止误触
                        //记得给gameManager设置棋子的选择！！！
                        //切记，很重要！！！
                        dameoThread.gameManager.setChoice(choice);
                    }
                }
                break;
            }
            case R.id.yellow0:case R.id.yellow1:case R.id.yellow2:case R.id.yellow3:
            {
                int playerid = dameoThread.gameManager.getTurn();
                int choice = chessToChoice(view.getId(),Chess.YELLOW);

                if(choice != -1)
                {
                    if(canClicked[playerid*4 + choice])
                    {
                        brokenAll();//防止误触
                        //记得给gameManager设置棋子的选择！！！
                        //切记，很重要！！！
                        dameoThread.gameManager.setChoice(choice);
                    }
                }
                break;
            }
            case R.id.blue0:case R.id.blue1:case R.id.blue2:case R.id.blue3:
            {
                int playerid = dameoThread.gameManager.getTurn();
                int choice = chessToChoice(view.getId(),Chess.BLUE);

                if(choice != -1)
                {
                    if(canClicked[playerid*4 + choice])
                    {
                        brokenAll();//防止误触
                        //记得给gameManager设置棋子的选择！！！
                        //切记，很重要！！！
                        dameoThread.gameManager.setChoice(choice);
                    }
                }
                break;
            }
            case R.id.green0:case R.id.green1:case R.id.green2:case R.id.green3:
            {
                int playerid = dameoThread.gameManager.getTurn();
                int choice = chessToChoice(view.getId(),Chess.GREEN);

                if(choice != -1)
                {
                    if(canClicked[playerid*4 + choice])
                    {
                        brokenAll();//防止误触
                        //记得给gameManager设置棋子的选择！！！
                        //切记，很重要！！！
                        dameoThread.gameManager.setChoice(choice);
                    }
                }
                break;
            }
            case R.id.switchMode:
            {
                //因为这里是单机游戏，所以定义单机游戏：只有玩家一（红色）才是人，其他都是AI

                //如果是我的回合
                if(dameoThread.gameManager.getTurn() == Chess.RED)
                {
                    //已经扔骰子了吗？

                    //还在等待骰子，说明还没扔骰子
                    if(dameoThread.gameManager.waitDice())
                    {
                        //如果以前是AI模式，说明现在需要取消托管
                        if(dameoThread.gameManager.isAI(Chess.RED))
                        {

                            //当前状态：现在是我的回合，并且我想切换到手动模式，并且还没有扔骰子

                            //直接显示扔骰子的按钮，让玩家自己扔就完事了,但是还得注意下：设置玩家不能选择棋子，以免误触

                            brokenAll();
                            throwDice.setVisibility(View.VISIBLE);
                            dameoThread.gameManager.switchToUser(Chess.RED);
                            switchMode.setText("托管");

                        }
                        //以前不是AI模式，说明现在要进行托管
                        else
                        {
                            //当前状态：现在是我的回合，并且我现在想切换到AI托管模式，并且我还没有扔骰子（即没点击那个扔骰子的按钮）

                            //下一步：直接取消显示扔骰子的按钮，并且随机生成一个骰子，并用gamemanager.setDice(),同时把设置玩家不能选择棋子

                            //顺序好像挺重要的，先切换，再setDice

                            brokenAll();
                            throwDice.setVisibility(View.INVISIBLE);
                            dameoThread.gameManager.switchToAI(Chess.RED);//先切换

                            int dice = dameoThread.newDice();

                            //播放扔骰子的动画
                            /****************************************/

                            //这里简单模拟一下这个过程

                            //Toast.makeText(MainActivity.this,"now,throwing a dice",Toast.LENGTH_SHORT).show();
                            showDice.setVisibility(View.VISIBLE);//先设置为可见
                            showDice.setText(diceToString(dice));
                            //showActionList.setVisibility(View.INVISIBLE);

                            /*****************************************/

                            dameoThread.gameManager.setDice(dice);//setDice 放最后
                            switchMode.setText("取消托管");
                        }
                    }
                    //已经扔了骰子
                    else
                    {
                        //还没选择棋子
                        if(dameoThread.gameManager.waitChoice())
                        {
                            //如果以前是AI模式，说明现在需要取消托管
                            if(dameoThread.gameManager.isAI(Chess.RED))
                            {
                                //当前状态：我想切换到手动模式，并且已经扔了骰子了，并且我还没有选择棋子
                                dameoThread.gameManager.switchToUser(Chess.RED);
                                switchMode.setText("托管");
                            }
                            //以前不是AI模式，说明现在要进行托管
                            else
                            {
                                //当前状态：我想切换到AI托管，并且我已经扔了骰子了，并且我还没有选择棋子

                                //下一步：让AI替你选择呗，先切换，再选择

                                dameoThread.gameManager.switchToAI(Chess.RED);

                                int playerid = dameoThread.gameManager.getTurn();
                                int choice = dameoThread.gameManager.getAIChoice();//AI选择一个棋子
                                brokenAll();

                                if(choice != -1)
                                {
                                    chesslist[playerid][choice].setBackgroundColor(buttonColor(playerid));
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this,"no choice",Toast.LENGTH_SHORT).show();
                                }

                                //最后要设置棋子
                                dameoThread.gameManager.setChoice(choice);
                                switchMode.setText("取消托管");
                            }
                        }
                        //已经选择棋子了
                        else
                        {
                            //如果以前是AI模式，说明现在需要取消托管
                            if(dameoThread.gameManager.isAI(Chess.RED))
                            {
                                dameoThread.gameManager.switchToUser(Chess.RED);
                                switchMode.setText("托管");
                            }
                            //以前不是AI模式，说明现在要进行托管
                            else{
                                dameoThread.gameManager.switchToAI(Chess.RED);
                                switchMode.setText("取消托管");
                            }
                        }

                    }

                }
                //如果不是我的回合，直接切换就好
                else
                {
                    //如果以前是AI模式，说明现在需要取消托管
                    if(dameoThread.gameManager.isAI(Chess.RED))
                    {
                        dameoThread.gameManager.switchToUser(Chess.RED);
                        switchMode.setText("托管");
                    }
                    //以前不是AI模式，说明现在要进行托管
                    else{
                        dameoThread.gameManager.switchToAI(Chess.RED);
                        switchMode.setText("取消托管");
                    }
                }
                break;
            }
        }
    }

    //把所有控件设置为不可用
    private void brokenAll()
    {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                chesslist[i][j].setBackgroundColor(Color.WHITE);//设置为白色，用来表示它不可以被选择
                canClicked[i*4+j] = false;//实际上通过设置canClicked数组来设置按钮能不能被点击
            }
        }
        throwDice.setVisibility(View.INVISIBLE);
        //showDice.setVisibility(View.INVISIBLE);
        //showActionList.setVisibility(View.INVISIBLE);
    }

    private void init()
    {
        chesslist = new Button[4][];

        for(int i = 0 ; i < 4; i++) {
            chesslist[i] = new Button[4];
        }

        chesslist[0][0] = (Button)(findViewById(R.id.red0));
        chesslist[0][1] = (Button)(findViewById(R.id.red1));
        chesslist[0][2] = (Button)(findViewById(R.id.red2));
        chesslist[0][3] = (Button)(findViewById(R.id.red3));

        chesslist[1][0] = (Button)(findViewById(R.id.yellow0));
        chesslist[1][1] = (Button)(findViewById(R.id.yellow1));
        chesslist[1][2] = (Button)(findViewById(R.id.yellow2));
        chesslist[1][3] = (Button)(findViewById(R.id.yellow3));

        chesslist[2][0] = (Button)(findViewById(R.id.blue0));
        chesslist[2][1] = (Button)(findViewById(R.id.blue1));
        chesslist[2][2] = (Button)(findViewById(R.id.blue2));
        chesslist[2][3] = (Button)(findViewById(R.id.blue3));

        chesslist[3][0] = (Button)(findViewById(R.id.green0));
        chesslist[3][1] = (Button)(findViewById(R.id.green1));
        chesslist[3][2] = (Button)(findViewById(R.id.green2));
        chesslist[3][3] = (Button)(findViewById(R.id.green3));

        turnView = (EditText) findViewById(R.id.turn);
        throwDice = (Button) findViewById(R.id.thrownDice);
        showDice = (EditText) findViewById(R.id.showdice);
        showActionList = (EditText) findViewById(R.id.actionLists);

        switchMode = (Button) findViewById(R.id.switchMode);//模式切换

        for(int i = 0 ; i < 4; i++){
            for(int j = 0 ; j < 4; j++)
            {
                chesslist[i][j].setOnClickListener(this);
            }
        }
        throwDice.setOnClickListener(this);
        switchMode.setOnClickListener(this);

        //设置为不可编辑
        turnView.setFocusable(false);
        turnView.setFocusableInTouchMode(false);
        turnView.setText(NOW + colorToString(Chess.RED) + PLAYER);

        showDice.setFocusable(false);
        showDice.setFocusableInTouchMode(false);
        showDice.setVisibility(View.INVISIBLE);

        showActionList.setFocusable(false);
        showActionList.setFocusableInTouchMode(false);
        showActionList.setVisibility(View.INVISIBLE);

        canClicked = new boolean[16];//16个棋子的按钮
        for(int i = 0 ; i < 16; i++) canClicked[i] = false;//初始化时默认棋子不可点击（即选择棋子)

//        dameoThread = new DameoThread(new GameManager(new PlayerAI(Chess.RED),
//                new AutoAI(Chess.YELLOW),new AutoAI(Chess.BLUE),new AutoAI(Chess.GREEN)));

        dameoThread = new DameoThread(new GameManager(new PlayerAI(Chess.RED),
                new AutoAI(Chess.YELLOW),new AutoAI(Chess.BLUE)));

//        dameoThread = new DameoThread(new GameManager(new PlayerAI(Chess.RED), new AutoAI(Chess.BLUE)));

        dameoThread.start();
    }

    /***********************************************/
    //以下为简单的转换函数

    private static String NOW = "现在轮到";
    private static String PLAYER = "玩家";


    private static String colorToString(int color)
    {
        if(color == Chess.RED) return "红色";
        else if(color == Chess.YELLOW) return "黄色";
        else if(color == Chess.BLUE) return "蓝色";
        else return "绿色";
    }
    private static String diceToString(int dice)
    {
        String str = "播放骰子动画:";
        if(dice == 1) return str + "点数一";
        else if(dice == 2) return str + "点数二";
        else if(dice == 3) return str + "点数三";
        else if(dice == 4) return str + "点数四";
        else if(dice == 5) return str + "点数五";
        else if(dice == 6) return str + "点数六";
        else return "点数错乱.";
    }
    private static int buttonColor(int player)
    {
        if(player == Chess.RED) return Color.RED;
        else if(player == Chess.YELLOW) return Color.YELLOW;
        else if(player == Chess.BLUE) return Color.BLUE;
        else return Color.GREEN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
}
