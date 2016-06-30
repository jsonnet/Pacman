package tileentity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import utilities.AssetLoader;
import utilities.MappingHelper;

/* define the maze */
public class TileentityMaze {
	// constant definitions
	public static final int BLANK = 0;
	public static final int WALL = 1;
	public static final int DOOR = 2;
	public static final int DOT = 4;
	public static final int POWER_DOT = 8;

	public static final int HEIGHT = 16;
	public static final int WIDTH = 21;

	public static final int iHeight = TileentityMaze.HEIGHT * 16;
	public static final int iWidth = TileentityMaze.WIDTH * 16;

	// the applet the object associate with
	Window applet;
	// the graphics it will be using
	Graphics graphics;

	// the maze image
	Image imageMaze;

	// the dot image
	Image imageDot;

	// total dots left
	public int iTotalDotcount;

	// the status of maze
	public int[][] iMaze;

	// initialize the maze
	public TileentityMaze(Window a, Graphics g) {
		// setup associations
		this.applet = a;
		this.graphics = g;

		this.imageMaze = this.applet.createImage(TileentityMaze.iWidth, TileentityMaze.iHeight);
		this.imageDot = this.applet.createImage(2, 2);

		// create data
		this.iMaze = new int[TileentityMaze.HEIGHT][TileentityMaze.WIDTH];
	}

	public void start() {
		int i, j, k;
		this.iTotalDotcount = 0;
		for (i = 0; i < TileentityMaze.HEIGHT; i++)
			for (j = 0; j < TileentityMaze.WIDTH; j++) {
				switch (MappingHelper.MazeDefine[i].charAt(j)) {
					case ' ':
						k = TileentityMaze.BLANK;
						break;
					case 'X':
						k = TileentityMaze.WALL;
						break;
					case '.':
						k = TileentityMaze.DOT;
						this.iTotalDotcount++;
						break;
					case 'O':
						k = TileentityMaze.POWER_DOT;
						break;
					case '-':
						k = TileentityMaze.DOOR;
						break;
					default:
						k = TileentityMaze.DOT;
						this.iTotalDotcount++;
						break;
				}
				this.iMaze[i][j] = k;
			}
		// create initial maze image
		this.createImage();
	}

	public void draw() {
		this.graphics.drawImage(this.imageMaze, 0, 0, this.applet);
		this.drawDots();
	}

	void drawDots() // on the offscreen
	{
		int i, j;

		for (i = 0; i < TileentityMaze.HEIGHT; i++)
			for (j = 0; j < TileentityMaze.WIDTH; j++)
				if (this.iMaze[i][j] == TileentityMaze.DOT)
					this.graphics.drawImage(this.imageDot, j * 16 + 7, i * 16 + 7, this.applet);
	}

	void createImage() {
		// create the image of a dot
		AssetLoader.drawDot(this.imageDot);

		// create the image of the maze
		Graphics gmaze = this.imageMaze.getGraphics();

		// background
		gmaze.setColor(Color.black);
		gmaze.fillRect(0, 0, TileentityMaze.iWidth, TileentityMaze.iHeight);

		this.DrawWall(gmaze);
	}

	public void DrawDot(int icol, int iRow) {
		if (this.iMaze[iRow][icol] == TileentityMaze.DOT)
			this.graphics.drawImage(this.imageDot, icol * 16 + 7, iRow * 16 + 7, this.applet);
	}

	void DrawWall(Graphics g) {
		int i, j;
		int iDir;

		g.setColor(Color.blue);

		for (i = 0; i < TileentityMaze.HEIGHT; i++)
			for (j = 0; j < TileentityMaze.WIDTH; j++)
				for (iDir = MappingHelper.RIGHT; iDir <= MappingHelper.DOWN; iDir++) {
					if (this.iMaze[i][j] == TileentityMaze.DOOR) {
						g.drawLine(j * 16, i * 16 + 8, j * 16 + 16, i * 16 + 8);
						continue;
					}
					if (this.iMaze[i][j] != TileentityMaze.WALL)
						continue;
					switch (iDir) {
						case MappingHelper.UP:
							if (i == 0)
								break;
							if (this.iMaze[i - 1][j] == TileentityMaze.WALL)
								break;
							this.DrawBoundary(g, j, i - 1, MappingHelper.DOWN);
							break;
						case MappingHelper.RIGHT:
							if (j == TileentityMaze.WIDTH - 1)
								break;
							if (this.iMaze[i][j + 1] == TileentityMaze.WALL)
								break;
							this.DrawBoundary(g, j + 1, i, MappingHelper.LEFT);
							break;
						case MappingHelper.DOWN:
							if (i == TileentityMaze.HEIGHT - 1)
								break;
							if (this.iMaze[i + 1][j] == TileentityMaze.WALL)
								break;
							this.DrawBoundary(g, j, i + 1, MappingHelper.UP);
							break;
						case MappingHelper.LEFT:
							if (j == 0)
								break;
							if (this.iMaze[i][j - 1] == TileentityMaze.WALL)
								break;
							this.DrawBoundary(g, j - 1, i, MappingHelper.RIGHT);
							break;
						default:
					}
				}
	}

	void DrawBoundary(Graphics g, int col, int row, int iDir) {
		int x, y;

		x = col * 16;
		y = row * 16;

		switch (iDir) {
			case MappingHelper.LEFT:
				// draw lower half segment
				if (this.iMaze[row + 1][col] != TileentityMaze.WALL)
					// down empty
					if (this.iMaze[row + 1][col - 1] != TileentityMaze.WALL)
						// arc(x-8,y+8,270,0,6);
						g.drawArc(x - 8 - 6, y + 8 - 6, 12, 12, 270, 100);
					else
					g.drawLine(x - 2, y + 8, x - 2, y + 16);
				else {
					g.drawLine(x - 2, y + 8, x - 2, y + 17);
					g.drawLine(x - 2, y + 17, x + 7, y + 17);
				}

				// Draw upper half segment
				if (this.iMaze[row - 1][col] != TileentityMaze.WALL)
					// upper empty
					if (this.iMaze[row - 1][col - 1] != TileentityMaze.WALL)
						// arc(x-8,y+7,0,90,6);
						g.drawArc(x - 8 - 6, y + 7 - 6, 12, 12, 0, 100);
					else
					g.drawLine(x - 2, y, x - 2, y + 7);
				else {
					g.drawLine(x - 2, y - 2, x - 2, y + 7);
					g.drawLine(x - 2, y - 2, x + 7, y - 2);
				}
				break;

			case MappingHelper.RIGHT:
				// draw lower half segment
				if (this.iMaze[row + 1][col] != TileentityMaze.WALL)
					// down empty
					if (this.iMaze[row + 1][col + 1] != TileentityMaze.WALL)
						// arc(x+16+7,y+8,180,270,6);
						g.drawArc(x + 16 + 7 - 6, y + 8 - 6, 12, 12, 180, 100);
					else
					g.drawLine(x + 17, y + 8, x + 17, y + 15);
				else {
					g.drawLine(x + 8, y + 17, x + 17, y + 17);
					g.drawLine(x + 17, y + 8, x + 17, y + 17);
				}
				// Draw upper half segment
				if (this.iMaze[row - 1][col] != TileentityMaze.WALL)
					// upper empty
					if (this.iMaze[row - 1][col + 1] != TileentityMaze.WALL)
						// arc(x+16+7,y+7,90,180,6);
						g.drawArc(x + 16 + 7 - 6, y + 7 - 6, 12, 12, 90, 100);
					else
					g.drawLine(x + 17, y, x + 17, y + 7);
				else {
					g.drawLine(x + 8, y - 2, x + 17, y - 2);
					g.drawLine(x + 17, y - 2, x + 17, y + 7);
				}
				break;

			case MappingHelper.UP:
				// draw left half segment
				if (this.iMaze[row][col - 1] != TileentityMaze.WALL)
					// left empty
					if (this.iMaze[row - 1][col - 1] != TileentityMaze.WALL)
						// arc(x+7,y-8,180,270,6);
						g.drawArc(x + 7 - 6, y - 8 - 6, 12, 12, 180, 100);
					else
					g.drawLine(x, y - 2, x + 7, y - 2);

				// Draw right half segment
				if (this.iMaze[row][col + 1] != TileentityMaze.WALL)
					// right empty
					if (this.iMaze[row - 1][col + 1] != TileentityMaze.WALL)
						// arc(x+8,y-8,270,0,6);
						g.drawArc(x + 8 - 6, y - 8 - 6, 12, 12, 270, 100);
					else
					g.drawLine(x + 8, y - 2, x + 16, y - 2);
				break;

			case MappingHelper.DOWN:
				// draw left half segment
				if (this.iMaze[row][col - 1] != TileentityMaze.WALL)
					// left empty
					if (this.iMaze[row + 1][col - 1] != TileentityMaze.WALL)
						// arc(x+7,y+16+7,90,180,6);
						g.drawArc(x + 7 - 6, y + 16 + 7 - 6, 12, 12, 90, 100);
					else
					g.drawLine(x, y + 17, x + 7, y + 17);

				// Draw right half segment
				if (this.iMaze[row][col + 1] != TileentityMaze.WALL)
					// right empty
					if (this.iMaze[row + 1][col + 1] != TileentityMaze.WALL)
						// arc(x+8,y+16+7,0,90,6);
						g.drawArc(x + 8 - 6, y + 16 + 7 - 6, 12, 12, 0, 100);
					else
					g.drawLine(x + 8, y + 17, x + 15, y + 17);
				break;
		}
	}
	// (c) 2016 Joshua Sonnet
}
