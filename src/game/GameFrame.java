package game;
import utils.Utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import static common.CommonConstants.*;

public class GameFrame extends Frame implements Runnable{
    //游戏状态
    private static int gameState;
    //菜单指向
    private static int menuIndex;
    //定义一张和屏幕大小一致的图片
    private final BufferedImage bufImg = new BufferedImage(FRAME_WIDTH,FRAME_HEIGHT,BufferedImage.TYPE_4BYTE_ABGR);
    //背景图片
    private Image backgroundImg = null;
    private static int score;  // 得分
    private static int shotsLeft;  // 剩余子弹
    private static int citiesLeft;  // 剩余城市
    private static int shieldLeft;  // 剩余护盾
    private static int enemyCount;  // 当前敌人数量
    private UFO[] ufos = new UFO[ENEMY_MAX_COUNT];
    private City[] cities = new City[CITY_COUNT];
    private static int lastEnemyBornTime = 0;  // 上次敌人出生时间
    private static final Random random = new Random();
    public GameFrame() {
        initFrame();
        initEventListener();
        // 初始化random
        random.setSeed(System.currentTimeMillis());
        //启动用于刷新窗口的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    repaint();
                    Utils.sleep(REPAINT_INTERVAL);
                }
            }
        }).start();
    }

    //初始化窗口
    private void initFrame() {
        //设置标题
        setTitle(GAME_TITLE);
        //设置窗口大小
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        //设置窗口的左上角的坐标
        setLocation(FRAME_X, FRAME_Y);
        //设置窗口大小不可改变
        setResizable(false);
        //设置窗口可见
        setVisible(true);
    }

    //添加按键监听事件
    private void initEventListener() {
        //注册监听事件
        addWindowListener(new WindowAdapter() {
            //点击关闭按钮的时候，方法会被自动调用
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //添加按键监听事件
        addKeyListener(new KeyAdapter() {
            //按键被按下的时候被回调的方法
            @Override
            public void keyPressed(KeyEvent e) {
                //获得被按下键的键值
                int keyCode = e.getKeyCode();
                //不同的游戏状态，给出不同的处理的方法。
                switch (gameState) {
                    case STATE_MENU:
                        keyPressedEventMenu(keyCode);
                        break;
                    case STATE_HELP:
                        keyPressedEventHelp();
                        break;
                    case STATE_ABOUT:
                        keyPressedEventAbout();
                        break;
                    case STATE_RUN:
                        keyPressedEventRun(keyCode);
                        break;
                    case STATE_LOST:
                        keyPressedEventLost(keyCode);
                        break;
                    case STATE_WIN:
                        keyPressedEventWin(keyCode);
                        break;
                }
            }

        });
        //添加鼠标监听事件,在游戏运行状态下，鼠标点击事件的处理
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(gameState == STATE_RUN) {
                    shotsLeft--;
                    for (int i = 0; i < ENEMY_MAX_COUNT; i++) {
                        if (ufos[i] != null && Utils.distanse(e.getX(), e.getY(), ufos[i].getX(), ufos[i].getY()) < DISROTY_UFO_DIS) {
                            ufos[i] = null;
                            score += DISROTY_UFO_SCORE;
                            break;
                        }
                    }
                }
            }
        });
    }

    //绘制菜单状态下的内容
    private void drawMenu(Graphics g) {
        //绘制背景图片
        if(backgroundImg == null){
            backgroundImg = Utils.getImage(BACKGROUND_IMG_PATH);
            // 缩放到窗口大小
            if (backgroundImg != null) {
                backgroundImg = backgroundImg.getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_DEFAULT);
            }
        }
        g.drawImage(backgroundImg, 0, 0, null);

        //绘制菜单项
        final int STR_WIDTH = 76;
        int x = FRAME_WIDTH - STR_WIDTH >> 1;
        int y = FRAME_HEIGHT / 3;
        //行间距
        final int DIS = 50;

        for (int i = 0; i < MENUS.length; i++) {
            if (i == menuIndex) {//选中的菜单项的颜色设置为红色。
                g.setColor(Color.RED);
            } else {//其他的为白色
                g.setColor(Color.WHITE);
            }
            g.drawString(MENUS[i], x, y + DIS * i);
        }
    }

    //绘制帮助状态下的内容
    private void drawHelp(Graphics g) {
        //绘制背景图片
        g.drawImage(backgroundImg, 0, 0, null);

        //绘制帮助内容
        g.setFont(GAME_FONT);
        g.setColor(Color.WHITE);
        final int STR_WIDTH = 76;
        int x = FRAME_WIDTH - STR_WIDTH >> 1;
        g.drawString("游戏帮助", x, 100);
        g.drawString("游戏目标：消灭所有UFO，保护城市不被摧毁", 100, 150);
        g.drawString("操作说明：使用鼠标点击出现的UFO ，一旦点", 100, 200);
        g.drawString("中某个UFO ，则将其击毁。", 225, 250);
        g.setColor(Color.RED);
        g.drawString("任意键返回主菜单", 310, 500);
    }

    //绘制关于状态下的内容
    private void drawAbout(Graphics g) {
        //绘制背景图片
        g.drawImage(backgroundImg, 0, 0, null);

        //绘制关于内容
        g.setFont(GAME_FONT);
        g.setColor(Color.WHITE);
        final int STR_WIDTH = 76;
        int x = FRAME_WIDTH - STR_WIDTH >> 1;
        g.drawString("游戏关于", x, 100);
        g.drawString("游戏名称：地球保卫战", 100, 150);
        g.drawString("游戏版本：1.0", 100, 200);
        g.drawString("游戏作者：江紫檀-2022141460178", 100, 250);
        g.setColor(Color.RED);
        g.drawString("任意键返回主菜单", 310, 500);

    }

    //绘制游戏运行状态下的内容
    private void drawRun(Graphics g) {
        //绘制相关的游戏内容
        g.drawImage(backgroundImg, 0, 0, null);
        g.setFont(GAME_FONT);
        g.setColor(Color.WHITE);
        g.drawString("得分：" + score, 20, 70);
        g.drawString("剩余城市：" + citiesLeft, 600, 70);
        g.drawString("剩余子弹：" + shotsLeft, 20, 100);
        g.drawString("剩余护盾：" + shieldLeft, 600, 100);

        //绘制城市，并判断城市是否被摧毁
        for (int i = 0; i < cities.length; i++) {
            if (cities[i] == null) {
                cities[i] = new City();
            }else{
                for(int j = 0; j < ENEMY_MAX_COUNT; j++) {
                    if (!cities[i].isDistory() && ufos[j] != null && ufos[j].getBullet() != null && Utils.distanse(cities[i].getX(), cities[i].getY(), ufos[j].getBullet().getX(), ufos[j].getBullet().getY()) < DISROTY_CITY_DIS) {
                        if(shieldLeft > 0) {
                            shieldLeft--;
                        }else{
                            cities[i].distory();
                            citiesLeft--;
                            score += DISROTY_CITY_SCORE;
                        }
                        ufos[j].setBullet(null);
                        break;
                    }
                }
                g.drawImage(Utils.getImage(cities[i].getImgPath()), cities[i].getX(), cities[i].getY(), null);
            }
        }

        //每隔5秒钟生成一个UFO
        if (enemyCount < ENEMY_MAX_COUNT && (lastEnemyBornTime == 0 || (int)System.currentTimeMillis() - lastEnemyBornTime > ENEMY_BORN_INTERVAL)) {
            ufos[enemyCount++] = new UFO(random.nextInt(100,600), random.nextInt(100,400), random.nextInt(3) + 1);
            lastEnemyBornTime = (int) System.currentTimeMillis();
        }
        for (int i = 0; i < ENEMY_MAX_COUNT; i++) {
            if (ufos[i] != null) {
                g.drawImage(Utils.getImage(ufos[i].getImgPath()), ufos[i].getX(), ufos[i].getY(), null);
                if(ufos[i].getBullet() != null) {
                    g.drawImage(Utils.getImage(ufos[i].getBullet().getImgPath()), ufos[i].getBullet().getX(), ufos[i].getBullet().getY(), null);
                }
            }
        }

        //判断游戏是否结束
        if(shotsLeft <= 0 || citiesLeft <= 0) {
            setGameState(STATE_LOST);
        }else if(enemyCount == ENEMY_MAX_COUNT) {
            boolean win = true;
            for (int i = 0; i < ENEMY_MAX_COUNT; i++) {
                if (ufos[i] != null) {
                    win = false;
                    break;
                }
            }
            if(win) {
                setGameState(STATE_WIN);
            }
        }
    }

    //绘制游戏结束状态下的内容
    private void drawLost(Graphics g) {
        g.drawImage(Utils.getImage(EARTHB_IMG_PATH), 0, 0, null);
        g.setFont(GAME_FONT);
        g.setColor(Color.RED);
        g.drawString("守护地球失败", 320, 200);
        //添加按键提示信息
        g.setColor(Color.WHITE);
        g.drawString(OVER_STR0,10,FRAME_HEIGHT-20);
        g.drawString(OVER_STR1,FRAME_WIDTH-200,FRAME_HEIGHT-20);
    }

    //绘制游戏通关状态下的内容
    private void drawWin(Graphics g){
        g.drawImage(Utils.getImage(EARTHA_IMG_PATH), 0, 0, null);
        g.setFont(GAME_FONT);
        g.setColor(Color.RED);
        g.drawString("守护地球成功", 320, 200);
        g.drawString("得分：" + score, 320, 350);
        //添加按键提示信息
        g.setColor(Color.WHITE);
        g.drawString(OVER_STR0,10,FRAME_HEIGHT-20);
        g.drawString(OVER_STR1,FRAME_WIDTH-200,FRAME_HEIGHT-20);
    }

    //菜单状态下的按键的处理
    private void keyPressedEventMenu(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (--menuIndex < 0) {
                    menuIndex = MENUS.length - 1;
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if(++menuIndex > MENUS.length -1){
                    menuIndex = 0;
                }
                break;
            case KeyEvent.VK_ENTER:
                switch(menuIndex){
                    case 0:
                        initGame();
                        break;
                    case 1:
                        setGameState(STATE_HELP);
                        break;
                    case 2:
                        setGameState(STATE_ABOUT);
                        break;
                    case 3:
                        System.exit(0);
                        break;
                }
                break;
        }
    }

    //帮助状态下的按键的处理
    private void keyPressedEventHelp() {
        setGameState(STATE_MENU);
    }

    //关于状态下的按键的处理
    private void keyPressedEventAbout() {
        setGameState(STATE_MENU);
    }

    //游戏运行中的按键处理方法
    private void keyPressedEventRun(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE){
            setGameState(STATE_MENU);
        }
    }

    //游戏结束的按键的处理
    private void keyPressedEventLost(int keyCode) {
        //结束游戏
        if(keyCode == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }else if(keyCode == KeyEvent.VK_ENTER){
            setGameState(STATE_MENU);
        }
    }

    //游戏通关的按键处理
    private void keyPressedEventWin(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }else if(keyCode == KeyEvent.VK_ENTER){
            setGameState(STATE_MENU);
        }
    }

    /**
     * 是Frame 类的方法，继承下来的方法，
     * 该方法负责了所有的绘制的内容，所有需要在屏幕中显式的
     * 内容，都要在该方法内调用。该方法不能主动调用。必须通过调用
     * repaint(); 去回调该方法。
     * @param g1 系统提供的画笔，系统进行初始化
     */
    public void update(Graphics g1) {
        Graphics g = bufImg.getGraphics();
        //根据不同的游戏状态，绘制不同的内容
        g.setFont(GAME_FONT);
        switch (gameState) {
            case STATE_MENU:
                drawMenu(g);
                break;
            case STATE_HELP:
                drawHelp(g);
                break;
            case STATE_ABOUT:
                drawAbout(g);
                break;
            case STATE_RUN:
                drawRun(g);
                break;
            case STATE_LOST:
                drawLost(g);
                break;
            case STATE_WIN:
                drawWin(g);
                break;
        }

        //使用系统画笔，将图片绘制到frame上来
        g1.drawImage(bufImg,0,0,null);
    }

    //初始化游戏
    private void initGame() {
        setGameState(STATE_RUN);
        score = ZERO;
        shotsLeft = INIT_BULLET_COUNT;
        citiesLeft = CITY_COUNT;
        shieldLeft = INIT_SHIELD_COUNT;
        enemyCount = ZERO;
        Arrays.fill(ufos, null);
        Arrays.fill(cities, null);
    }

    //修改游戏状态
    public static void setGameState(int gameState) {
        GameFrame.gameState = gameState;
    }

    //启动用于刷新窗口的线程
    @Override
    public void run() {
        while(true){
            //在此调用repaint,回调update
            repaint();
            Utils.sleep(REPAINT_INTERVAL);
        }
    }
}
