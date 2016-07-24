package com.tarena.tetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

/**俄罗斯阿方块面板*/
public class Tetris extends JPanel{
	//分数，墙，正在下落方块，下一个方块 
	public static final int ROWS=20;//墙行数
	public static final int COLS=10;//墙列数
	private int score;//分数
	private int lines;//销毁的行数
	private Cell[][] wall;
	private Tetromino tetromino;//正在下落方块
	private Tetromino nextOne;//下一个要下落的方块
	//图片
	public static BufferedImage background;
	public static BufferedImage T;
	public static BufferedImage S;
	public static BufferedImage Z;
	public static BufferedImage L;
	public static BufferedImage J;
	public static BufferedImage I;
	public static BufferedImage O;
	public static BufferedImage Game_over;
	static {
		try{//异常捕获
			background=ImageIO.read(Tetris.class.getResource("tetris.png"));//读取图片
			T=ImageIO.read(Tetris.class.getResource("T.png"));//读取图片
			S=ImageIO.read(Tetris.class.getResource("S.png"));//读取图片
			Z=ImageIO.read(Tetris.class.getResource("Z.png"));//读取图片
			L=ImageIO.read(Tetris.class.getResource("L.png"));//读取图片
			J=ImageIO.read(Tetris.class.getResource("J.png"));//读取图片
			I=ImageIO.read(Tetris.class.getResource("I.png"));//读取图片
			O=ImageIO.read(Tetris.class.getResource("O.png"));//读取图片
			Game_over=ImageIO.read(Tetris.class.getResource("game_over.png"));//读取图片
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**tetris面板操作*/
	public void action(){
		wall =new Cell[ROWS][COLS];
		startAction();
		tetromino=Tetromino.randomOne();
		nextOne=Tetromino.randomOne();
		
		//舰艇键盘事件，创建监听器对象，注册监听器
		
		KeyAdapter l=new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				int Key =e.getKeyCode();
				
				if (Key == KeyEvent.VK_Q) {//Q表示退出
                    System.exit(0);// 结束Java进程
                }
                if (gameOver) {
                    if (Key == KeyEvent.VK_S) {//S表示开始
                        startAction();
                        repaint();
                    }
                    return;
                }
                if (pause) {// pause = true
                    if (Key == KeyEvent.VK_C) {//C表示继续
                        continueAction();
                        repaint();
                    }
                    return;
                }
                
				switch(Key){
					case KeyEvent.VK_LEFT:
						moveLeftAction();
						break;
					case KeyEvent.VK_RIGHT:
						moveRightAction();
						break;
					case KeyEvent.VK_DOWN:
						softDropAction();
						break;
					case KeyEvent.VK_SPACE:
						hardDropAction();
						break;
					case KeyEvent.VK_UP:
						rotateRightAction();
						break;
					case KeyEvent.VK_P://按键盘上的P表示暂停
	                    pauseAction();
	                    break;
				}
				repaint();
			}
		};
		this.requestFocus();
		this.addKeyListener(l);
	}
	
	//在tertis类中，重写绘制方法，绘制背景图片
			/**绘制*/
			@Override
	public void paint(Graphics g) {
			g.drawImage(background,0,0,null);//(图片，对齐横坐标,对齐纵坐标，null)
			g.translate(15, 15);//坐标系平移
			paintWall(g);//画墙
			paintTetromino(g);//画正在下落的格子
			paintnextOne(g);//画下一个下落的格子
			paintScore(g);//绘制分数
			if(gameOver){
				g.drawImage(Game_over,0,0,null);
			}
		}
	
	
	public static final int FONT_COLOR=0x667799;
	public static final int FONT_SIZE=30;
	/**绘制分数*/
	private void paintScore(Graphics g){
		int x=290;
		int y=160;
		g.setColor(new Color(FONT_COLOR));
		Font font=g.getFont();//取得g当前字体
		font =new Font(font.getName(),font.getStyle(),FONT_SIZE);
		g.setFont(font);
		String str="SCORE"+score;
		g.drawString(str,x,y);
		y+=56;
		str="LINES:"+lines;
		g.drawString(str, x, y);
		y += 56;
        str = "[P]Pause";
        if (pause) {
            str = "[C]Continue";
        }
        if (gameOver) {
            str = "[S]Start!";
        }
        g.drawString(str, x, y);
	}
	
	
	
	/**画格子*/
	/**下落的方块*/
	public void paintTetromino(Graphics g){
		if(tetromino==null)
		{
			return;
		}
		Cell[] cells =tetromino.cells;
		for(int i=0;i<cells.length;i++){
			Cell cell=cells[i];
			int x=cell.getCol()*CELL_SIZE;
			int y=cell.getRow()*CELL_SIZE;
			g.drawImage(cell.getImage(), x, y,null);
		}
	}
	/**下一个要下落的方块*/
	public void paintnextOne(Graphics g){
		if(nextOne==null)
		{
			return;
		}
		Cell[] cells =nextOne.cells;
		for(int i=0;i<cells.length;i++){
			Cell cell=cells[i];
			int x=(cell.getCol()+10)*CELL_SIZE;
			int y=(cell.getRow()+1)*CELL_SIZE;
			g.drawImage(cell.getImage(), x, y,null);
		}
	}
	public static final int CELL_SIZE=26;
	/**画墙*/
	public void paintWall(Graphics g){
		for(int row =0;row<wall.length;row++){
			Cell[]line =wall[row];
			for(int col=0;col<line.length;col++){
				Cell cell =line[col];
				int x=col*CELL_SIZE;
				int y=row*CELL_SIZE;
				if(cell==null){
					g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
				}else{
					g.drawImage(cell.getImage(), x-1, y-1, null);
				}
			}
		}
	}
	/**判断是否出界*/
	public boolean outBounds(){
		Cell[] cells =tetromino.cells;
		for(int i=0;i<cells.length;i++){
			Cell cell=cells[i];
			int col=cell.getCol();
			if(col < 0 || col >= COLS){
				return true;
			}
		}
		return false;
	}
	/**检查正在下落的方块是否与墙上与砖块重叠*/
	public boolean cioncide(){
		Cell[] cells =tetromino.cells;
		for(int i=0;i<cells.length;i++)
		{
			Cell cell =cells[i];
			int row=cell.getRow();
			int col=cell.getCol();
			//如果墙的row，col位置上有格子，就重叠了
			if(row >= 0 && row < ROWS && col >= 0 && col <= COLS&& wall[row][col] != null)
			{
				return true;//重叠
			}
		}
		return false;
	}
	/**右移动计算流程处理，要处理出界策略*/
	public void moveRightAction(){
		tetromino.moveRight();
		if(outBounds()||cioncide()){
			tetromino.moveLeft();
		}
	}
	
	/**左移动计算流程处理，要处理出界策略*/
	public void moveLeftAction(){
		tetromino.moveLeft();
		if(outBounds()||cioncide()){
			tetromino.moveRight();
		}
	}
	
	/**下落控制流*/
	public void softDropAction(){
        if (canDrop()) {
            tetromino.softDrop();
        } else {
            landIntoWall();
            destoryLines();
            checkGameOverAction();
            tetromino = nextOne;
            nextOne = Tetromino.randomOne();
        }
	}
	/**硬下落流程，下落到不能下落位置，绑定到空格键事件上*/
	public void hardDropAction(){
        while (canDrop()) {
            tetromino.softDrop();
        }
        landIntoWall();
        destoryLines();
        checkGameOverAction();
        tetromino = nextOne;
        nextOne = Tetromino.randomOne();
	}
	/** 在Tetris类中添加 旋转流程控制方法 */
    public void rotateRightAction() {
        tetromino.rotateRight();
        if (outBounds() ||cioncide()){
            tetromino.rotateLeft();
        }
    }
	
	private static int[]scoreTable={0,1,10,50,100};
	private void destoryLines() {
		int lines = 0;
        for (int row = 0; row < wall.length; row++) {
            if (fullCells(row)) {
                deleteRow(row);
                lines++;
            }
        }
        this.score += scoreTable[lines];
        this.lines += lines;
	}
	private void deleteRow(int row) {
        for (int i = row; i >= 1; i--) {
            System.arraycopy(wall[i - 1], 0, wall[i], 0, COLS);
        }
        Arrays.fill(wall[0], null);
	}
	/**检查当前行的每个格子,是否是满的，如果满了则返回true，否则返回false*/
	private boolean fullCells(int row) {
		Cell[]line=wall[row];
		for(Cell cell:line){
			if(cell==null){
				return false;
				}
		}
		return true;
	}
	private void landIntoWall() {
		Cell[]cells=tetromino.cells;
		for(int i=0;i<cells.length;i++){
			Cell cell=cells[i];
			int row=cell.getRow();
			int col=cell.getCol();
			wall[row][col]=cell;
		}
	}
	/**检查当前下落方块啊是否能够下落，能下落返回true*/
	public boolean canDrop() {
		Cell[]cells=tetromino.cells;
		for(int i=0;i<cells.length;i++){
			Cell cell=cells[i];
			int row=cell.getRow();
			if(row==ROWS-1){
				return false;
			}
		}
		for(Cell cell : cells){
			int row=cell.getRow()+1;
			int col=cell.getCol();
			if(row >= 0 && row < ROWS && col >= 0 && col <= COLS&& wall[row][col] != null){
				return false;
			}
		}
		return true;
	}
	
	private Timer timer;
    private boolean pause;//是否为暂停状态
    private boolean gameOver;//是否为游戏结束状态
    private long interval = 600;// 间隔时间
    /** 在Tetris类中添加 开始流程控制 */
    public void startAction() {
        pause = false;
        gameOver = false;
        score = 0;
        lines = 0;
        clearWall();
        tetromino = Tetromino.randomOne();
        nextOne = Tetromino.randomOne();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                softDropAction();
                repaint();
            }
        }, interval, interval);
    }
    /**清除墙上的方块*/
    private void clearWall() {
        for (Cell[] line : wall) {
            Arrays.fill(line, null);
        }
    }
    /**暂停*/
    public void pauseAction() {
        timer.cancel();
        pause = true;
    }
    /**继续*/
    public void continueAction() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                softDropAction();
                repaint();
            }
        }, interval, interval);
        pause = false;
    }
    /**游戏结束*/
    public void checkGameOverAction() {
        if (wall[0][4] != null) {
            gameOver = true;
            timer.cancel();
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        // 在加载Tetris类的时候, 会执行静态代码块
        // 静态代码块,装载了图片素材, 为图片对象
        Tetris tetris = new Tetris();
        //将面板的颜色设置为蓝色，用于测试
        tetris.setBackground(new Color(0x0000ff));
        frame.add(tetris);
        frame.setSize(530, 580);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);// 在显示窗口时候,会"尽快"的调用paint()方法绘制界面
        tetris.action();
    }
}
