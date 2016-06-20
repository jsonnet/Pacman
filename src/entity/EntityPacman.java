package entity;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import tileentity.TileentityMaze;
import tileentity.TileentityPowerdot;
import utilities.AssetLoader;
import utilities.MappingHelper;

public class EntityPacman {
	// frames to wait after eaten a dot
	final int DOT_WAIT = 4;

	int iDotWait;

	// current position
	public int iX, iY;

	// current direction
	public int iDir;

	// the applet this object is associated to
	Window applet;
	Graphics graphics;

	// the pac image
	public Image[][] imagePac;

	// the knowledge of the maze
	TileentityMaze maze;

	// the knowledge of the power dots
	TileentityPowerdot powerDot;

	// cpacmove cAuto;

	// cpac(Window a, Graphics g, cmaze m, cpowerdot p, cghost cghost[])
	public EntityPacman(Window a, Graphics g, TileentityMaze m, TileentityPowerdot p) {
		this.applet = a;
		this.graphics = g;
		this.maze = m;
		this.powerDot = p;

		// initialize pac and pac image
		this.imagePac = new Image[4][4];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				this.imagePac[i][j] = this.applet.createImage(18, 18);
				AssetLoader.drawPac(this.imagePac[i][j], i, j);
			}
	}

	public void start() {
		this.setiX(10 * 16);
		this.iY = 10 * 16;
		this.iDir = 1; // downward, illegal and won't move
		this.iDotWait = 0;
	}

	public void draw() {
		this.maze.DrawDot(this.getiX() / 16, this.iY / 16);
		this.maze.DrawDot(this.getiX() / 16 + (this.getiX() % 16 > 0 ? 1 : 0), this.iY / 16 + (this.iY % 16 > 0 ? 1 : 0));

		int iImageStep = (this.getiX() % 16 + this.iY % 16) / 2; // determine shape of PAc
		if (iImageStep < 4)
			iImageStep = 3 - iImageStep;
		else
			iImageStep -= 4;
		this.graphics.drawImage(this.imagePac[this.iDir][iImageStep], this.getiX() - 1, this.iY - 1, this.applet);
	}

	// return 1 if eat a dot
	// return 2 if eat power dot
	public int move(int iNextDir) {
		int eaten = 0;

		// iNextDir=cAuto.GetNextDir();

		if (iNextDir != -1 && iNextDir != this.iDir)
			if (this.getiX() % 16 != 0 || this.iY % 16 != 0) {
				// only check go back
				if (iNextDir % 2 == this.iDir % 2)
					this.iDir = iNextDir;
			} else if (this.mazeOK(this.getiX() / 16 + MappingHelper.iXDirection[iNextDir], this.iY / 16 + MappingHelper.iYDirection[iNextDir])) {
				this.iDir = iNextDir;
				iNextDir = -1;
			}
		if (this.getiX() % 16 == 0 && this.iY % 16 == 0) {

			// see whether has eaten something
			switch (this.maze.iMaze[this.iY / 16][this.getiX() / 16]) {
				case TileentityMaze.DOT:
					eaten = 1;
					this.maze.iMaze[this.iY / 16][this.getiX() / 16] = TileentityMaze.BLANK; // eat dot
					this.maze.iTotalDotcount--;
					this.iDotWait = this.DOT_WAIT;
					break;
				case TileentityMaze.POWER_DOT:
					eaten = 2;
					this.powerDot.eat(this.getiX() / 16, this.iY / 16);
					this.maze.iMaze[this.iY / 16][this.getiX() / 16] = TileentityMaze.BLANK;
					break;
			}

			if (this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[this.iDir]][this.getiX() / 16 + MappingHelper.iXDirection[this.iDir]] == 1)
				return (eaten); // not valid move
		}
		if (this.iDotWait == 0) {
			this.setiX(this.getiX() + MappingHelper.iXDirection[this.iDir]);
			this.iY += MappingHelper.iYDirection[this.iDir];
		} else
			this.iDotWait--;
		return (eaten);
	}

	public boolean mazeOK(int iRow, int icol) {
		if ((this.maze.iMaze[icol][iRow] & (TileentityMaze.WALL | TileentityMaze.DOOR)) == 0)
			return (true);
		return (false);
	}

	public int getiX() {
		return this.iX;
	}

	public void setiX(int iX) {
		this.iX = iX;
	}
}
