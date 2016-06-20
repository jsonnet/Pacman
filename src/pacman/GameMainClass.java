package pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import entity.EntityGhost;
import entity.EntityPacman;
import tileentity.TileentityMaze;
import tileentity.TileentityPowerdot;
import utilities.MappingHelper;

/**
 * the main class of the pacman game
 */
public class GameMainClass extends Frame implements Runnable, KeyListener, ActionListener, WindowListener {
	private static final long serialVersionUID = 3582431359568375379L;
	// the timer
	Thread timer;
	int timerPeriod = 12; // in miliseconds

	// the timer will increment this variable to signal a frame
	int signalMove = 0;

	// for graphics
	final int canvasWidth = 368;
	final int canvasHeight = 288 + 1;

	// the canvas starting point within the frame
	int topOffset;
	int leftOffset;

	// the draw point of maze within the canvas
	final int iMazeX = 16;
	final int iMazeY = 16;

	// the off screen canvas for the maze
	Image offScreen;
	Graphics offScreenG;

	// the objects
	TileentityMaze maze;
	EntityPacman pac;
	TileentityPowerdot powerDot;
	EntityGhost[] ghosts;

	// game counters
	final int PAcLIVE = 3;
	int pacRemain;
	int changePacRemain; // to signal redraw remaining pac

	// score
	int score;
	int hiScore;
	int scoreGhost; // score of eat ghost, doubles every time
	int changeScore; // signal score change
	int changeHiScore; // signal change of hi score

	// score images
	Image imgScore;
	Graphics imgScoreG;
	Image imgHiScore;
	Graphics imgHiScoreG;

	// game status
	final int INITIMAGE = 100; // need to wait before paint anything
	final int STARTWAIT = 0; // wait before running
	final int RUNNING = 1;
	final int DEADWAIT = 2; // wait after dead
	final int SUSPENDED = 3; // suspended during game
	int gameState;

	final int WAITCOUNT = 100; // 100 frames for wait states
	int wait; // the counter

	// rounds
	int round; // the round of current game;

	// whether it is played in a new maze
	boolean newMaze;

	// the direction specified by key
	int pacKeyDir;
	int key = 0;
	final int NONE = 0;
	final int SUSPEND = 1; // stop/start
	final int BOSS = 2; // boss

	////////////////////////////////////////////////
	// initialize the object
	// only called once at the beginning
	////////////////////////////////////////////////
	public GameMainClass() {
		super("PACMAN");

		// init variables
		this.hiScore = 0;

		this.gameState = this.INITIMAGE;

		this.initGUI();

		this.addWindowListener(this);

		this.addKeyListener(this);

		this.setSize(this.canvasWidth, this.canvasHeight);

		this.setVisible(true);

		// System.out.println("cpcman done");

	}

	void initGUI() {
		this.addNotify(); // for updated inset information

		// System.out.println("initGUI done.");
	}

	public void initImages() {
		// initialize off screen drawing canvas
		this.offScreen = this.createImage(TileentityMaze.iWidth, TileentityMaze.iHeight);
		if (this.offScreen == null)
			System.out.println("createImage failed");
		this.offScreenG = this.offScreen.getGraphics();

		// initialize maze object
		this.maze = new TileentityMaze(this, this.offScreenG);

		// initialize ghosts object
		// 4 ghosts
		this.ghosts = new EntityGhost[4];
		for (int i = 0; i < 4; i++) {
			Color color;
			if (i == 0)
				color = Color.red;
			else if (i == 1)
				color = Color.blue;
			else if (i == 2)
				color = Color.white;
			else
				color = Color.orange;
			this.ghosts[i] = new EntityGhost(this, this.offScreenG, this.maze, color);
		}

		// initialize power dot object
		this.powerDot = new TileentityPowerdot(this, this.offScreenG, this.ghosts);

		// initialize pac object
		// pac = new cpac(this, offScreenG, maze, powerDot, ghosts);
		this.pac = new EntityPacman(this, this.offScreenG, this.maze, this.powerDot);

		// initialize the score images
		this.imgScore = this.createImage(150, 16);
		this.imgScoreG = this.imgScore.getGraphics();
		this.imgHiScore = this.createImage(150, 16);
		this.imgHiScoreG = this.imgHiScore.getGraphics();

		this.imgHiScoreG.setColor(Color.black);
		this.imgHiScoreG.fillRect(0, 0, 150, 16);
		this.imgHiScoreG.setColor(Color.red);
		this.imgHiScoreG.setFont(new Font("Helvetica", Font.BOLD, 12));
		this.imgHiScoreG.drawString("HI SCORE", 0, 14);

		this.imgScoreG.setColor(Color.black);
		this.imgScoreG.fillRect(0, 0, 150, 16);
		this.imgScoreG.setColor(Color.green);
		this.imgScoreG.setFont(new Font("Helvetica", Font.BOLD, 12));
		this.imgScoreG.drawString("SCORE", 0, 14);
	}

	void startTimer() {
		// start the timer
		this.timer = new Thread(this);
		this.timer.start();
	}

	void startGame() {
		this.pacRemain = this.PAcLIVE;
		this.changePacRemain = 1;

		this.score = 0;
		this.changeScore = 1;

		this.newMaze = true;

		this.round = 1;

		this.startRound();
	}

	void startRound() {
		// new round for maze?
		if (this.newMaze == true) {
			this.maze.start();
			this.powerDot.start();
			this.newMaze = false;
		}

		this.maze.draw(); // draw maze in off screen buffer

		this.pac.start();
		this.pacKeyDir = MappingHelper.DOWN;
		for (int i = 0; i < 4; i++)
			this.ghosts[i].start(i, this.round);

		this.gameState = this.STARTWAIT;
		this.wait = this.WAITCOUNT;
	}

	///////////////////////////////////////////
	// paint everything
	///////////////////////////////////////////
	@Override
	public void paint(Graphics g) {
		if (this.gameState == this.INITIMAGE) {
			// System.out.println("first paint(...)...");

			// init images, must be done after show() because of Graphics
			this.initImages();

			// set the proper size of canvas
			Insets insets = this.getInsets();

			this.topOffset = insets.top;
			this.leftOffset = insets.left;

			// System.out.println(topOffset);
			// System.out.println(leftOffset);

			this.setSize(this.canvasWidth + insets.left + insets.right, this.canvasHeight + insets.top + insets.bottom);

			this.setResizable(false);

			// now we can start timer
			this.startGame();

			this.startTimer();

		}

		g.setColor(Color.black);
		g.fillRect(this.leftOffset, this.topOffset, this.canvasWidth, this.canvasHeight);

		this.changeScore = 1;
		this.changeHiScore = 1;
		this.changePacRemain = 1;

		this.paintUpdate(g);
	}

	void paintUpdate(Graphics g) {
		// updating the frame

		this.powerDot.draw();

		for (int i = 0; i < 4; i++)
			this.ghosts[i].draw();

		this.pac.draw();

		// display the offscreen
		g.drawImage(this.offScreen, this.iMazeX + this.leftOffset, this.iMazeY + this.topOffset, this);

		// display extra information
		if (this.changeHiScore == 1) {
			this.imgHiScoreG.setColor(Color.black);
			this.imgHiScoreG.fillRect(70, 0, 80, 16);
			this.imgHiScoreG.setColor(Color.red);
			this.imgHiScoreG.drawString(Integer.toString(this.hiScore), 70, 14);
			g.drawImage(this.imgHiScore, 8 + this.leftOffset, 0 + this.topOffset, this);

			this.changeHiScore = 0;
		}

		if (this.changeScore == 1) {
			this.imgScoreG.setColor(Color.black);
			this.imgScoreG.fillRect(70, 0, 80, 16);
			this.imgScoreG.setColor(Color.green);
			this.imgScoreG.drawString(Integer.toString(this.score), 70, 14);
			g.drawImage(this.imgScore, 158 + this.leftOffset, 0 + this.topOffset, this);

			this.changeScore = 0;
		}

		// update pac life info
		if (this.changePacRemain == 1) {
			int i;
			for (i = 1; i < this.pacRemain; i++)
				g.drawImage(this.pac.imagePac[0][0], 16 * i + this.leftOffset, this.canvasHeight - 18 + this.topOffset, this);
			g.drawImage(this.powerDot.imageBlank, 16 * i + this.leftOffset, this.canvasHeight - 17 + this.topOffset, this);

			this.changePacRemain = 0;
		}
	}

	////////////////////////////////////////////////////////////
	// controls moves
	// this is the routine running at the background of drawings
	////////////////////////////////////////////////////////////
	void move() {
		int k;

		int oldScore = this.score;

		for (int i = 0; i < 4; i++)
			this.ghosts[i].move(this.pac.getiX(), this.pac.iY, this.pac.iDir);

		k = this.pac.move(this.pacKeyDir);

		if (k == 1) // eaten a dot
		{
			this.changeScore = 1;
			this.score += 10 * ((this.round + 1) / 2);
		} else if (k == 2)
			this.scoreGhost = 200;

		if (this.maze.iTotalDotcount == 0) {
			this.gameState = this.DEADWAIT;
			this.wait = this.WAITCOUNT;
			this.newMaze = true;
			this.round++;
			return;
		}

		for (int i = 0; i < 4; i++) {
			k = this.ghosts[i].testCollision(this.pac.getiX(), this.pac.iY);
			if (k == 1) // kill pac
			{
				this.pacRemain--;
				this.changePacRemain = 1;
				this.gameState = this.DEADWAIT; // stop the game
				this.wait = this.WAITCOUNT;
				return;
			} else if (k == 2) // caught by pac
			{
				this.score += this.scoreGhost * ((this.round + 1) / 2);
				this.changeScore = 1;
				this.scoreGhost *= 2;
			}
		}

		if (this.score > this.hiScore) {
			this.hiScore = this.score;
			this.changeHiScore = 1;
		}

		if (this.changeScore == 1)
			if (this.score / 10000 - oldScore / 10000 > 0) {
				this.pacRemain++; // bonus
				this.changePacRemain = 1;
			}
	}

	///////////////////////////////////////////
	// this is the routine draw each frames
	///////////////////////////////////////////
	@Override
	public void update(Graphics g) {
		// System.out.println("update called");
		if (this.gameState == this.INITIMAGE)
			return;

		// seperate the timer from update
		if (this.signalMove != 0) {
			// System.out.println("update by timer");
			this.signalMove = 0;

			if (this.wait != 0) {
				this.wait--;
				return;
			}

			switch (this.gameState) {
				case STARTWAIT:
					if (this.pacKeyDir == MappingHelper.UP) // the key to start game
						this.gameState = this.RUNNING;
					else
						return;
					break;
				case RUNNING:
					if (this.key == this.SUSPEND)
						this.gameState = this.SUSPENDED;
					else
						this.move();
					break;
				case DEADWAIT:
					if (this.pacRemain > 0)
						this.startRound();
					else
						this.startGame();
					this.gameState = this.STARTWAIT;
					this.wait = this.WAITCOUNT;
					this.pacKeyDir = MappingHelper.DOWN;
					break;
				case SUSPENDED:
					if (this.key == this.SUSPEND)
						this.gameState = this.RUNNING;
					break;
			}
			this.key = this.NONE;
		}

		this.paintUpdate(g);
	}

	///////////////////////////////////////
	// process key input
	///////////////////////////////////////
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_L:
				this.pacKeyDir = MappingHelper.RIGHT;
				// e.consume();
				break;
			case KeyEvent.VK_UP:
				this.pacKeyDir = MappingHelper.UP;
				// e.consume();
				break;
			case KeyEvent.VK_LEFT:
				this.pacKeyDir = MappingHelper.LEFT;
				// e.consume();
				break;
			case KeyEvent.VK_DOWN:
				this.pacKeyDir = MappingHelper.DOWN;
				// e.consume();
				break;
			case KeyEvent.VK_S:
				this.key = this.SUSPEND;
				break;
			case KeyEvent.VK_B:
				this.key = this.BOSS;
				break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	/////////////////////////////////////////////////
	// handles menu event
	/////////////////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent e) {
	}

	///////////////////////////////////////////////////
	// handles window event
	///////////////////////////////////////////////////
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		this.dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	/////////////////////////////////////////////////
	// the timer
	/////////////////////////////////////////////////
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(this.timerPeriod);
			} catch (InterruptedException e) {
				return;
			}

			this.signalMove++;
			this.repaint();
		}
	}

	// for applet the check state
	boolean finalized = false;

	@Override
	public void dispose() {
		// timer.stop(); // deprecated
		// kill the thread
		this.timer.interrupt();

		// the off screen canvas
		// Image offScreen=null;
		this.offScreenG.dispose();
		this.offScreenG = null;

		// the objects
		this.maze = null;
		this.pac = null;
		this.powerDot = null;
		for (int i = 0; i < 4; i++)
			this.ghosts[i] = null;
		this.ghosts = null;

		// score images
		this.imgScore = null;
		this.imgHiScore = null;
		this.imgScoreG.dispose();
		this.imgScoreG = null;
		this.imgHiScoreG.dispose();
		this.imgHiScoreG = null;

		super.dispose();

		this.finalized = true;
	}

	public boolean isFinalized() {
		return this.finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
}
