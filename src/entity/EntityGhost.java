package entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import tileentity.TileentityMaze;
import utilities.AssetLoader;
import utilities.Helper;
import utilities.MappingHelper;
import utilities.SpeedControl;

public class EntityGhost {
	final int IN = 0;
	final int OUT = 1;
	public final int BLIND = 2;
	final int EYE = 3;

	final int[] steps = { 7, 7, 1, 1 };
	final int[] frames = { 8, 8, 2, 1 };

	final int INIT_BLIND_COUNT = 600; // remain blind for ??? frames
	int blindCount;

	SpeedControl speed = new SpeedControl();

	public int iX, iY, iDir;
	public int iStatus;
	int iBlink, iBlindCount;

	// random calculation factors
	final int DIR_FACTOR = 2;
	final int POS_FACTOR = 10;

	// the applet this object is associated to
	Window applet;
	Graphics graphics;

	// the maze the ghosts knows
	TileentityMaze maze;

	// the ghost image
	Image imageGhost;
	Image imageBlind;
	Image imageEye;

	public EntityGhost(Window a, Graphics g, TileentityMaze m, Color color) {
		this.applet = a;
		this.graphics = g;
		this.maze = m;

		this.imageGhost = this.applet.createImage(18, 18);
		AssetLoader.drawGhost(this.imageGhost, 0, color);

		this.imageBlind = this.applet.createImage(18, 18);
		AssetLoader.drawGhost(this.imageBlind, 1, Color.white);

		this.imageEye = this.applet.createImage(18, 18);
		AssetLoader.drawGhost(this.imageEye, 2, Color.lightGray);
	}

	public void start(int initialPosition, int round) {
		if (initialPosition >= 2)
			initialPosition++;
		this.iX = (8 + initialPosition) * 16;
		this.iY = 8 * 16;
		this.iDir = 3;
		this.iStatus = this.IN;

		this.blindCount = this.INIT_BLIND_COUNT / ((round + 1) / 2);

		this.speed.start(this.steps[this.iStatus], this.frames[this.iStatus]);
	}

	public void draw() {
		this.maze.DrawDot(this.iX / 16, this.iY / 16);
		this.maze.DrawDot(this.iX / 16 + (this.iX % 16 > 0 ? 1 : 0), this.iY / 16 + (this.iY % 16 > 0 ? 1 : 0));

		if (this.iStatus == this.BLIND && this.iBlink == 1 && this.iBlindCount % 32 < 16)
			this.graphics.drawImage(this.imageGhost, this.iX - 1, this.iY - 1, this.applet);
		else if (this.iStatus == this.OUT || this.iStatus == this.IN)
			this.graphics.drawImage(this.imageGhost, this.iX - 1, this.iY - 1, this.applet);
		else if (this.iStatus == this.BLIND)
			this.graphics.drawImage(this.imageBlind, this.iX - 1, this.iY - 1, this.applet);
		else
			this.graphics.drawImage(this.imageEye, this.iX - 1, this.iY - 1, this.applet);
	}

	public void move(int iPacX, int iPacY, int iPacDir) {
		if (this.iStatus == this.BLIND) {
			this.iBlindCount--;
			if (this.iBlindCount < this.blindCount / 3)
				this.iBlink = 1;
			if (this.iBlindCount == 0)
				this.iStatus = this.OUT;
			if (this.iBlindCount % 2 == 1) // blind moves at 1/2 speed
				return;
		}

		if (this.speed.isMove() == 0)
			// no move
			return;

		if (this.iX % 16 == 0 && this.iY % 16 == 0)
			switch (this.iStatus) {
				case IN:
					this.iDir = this.INSelect();
					break;
				case OUT:
					this.iDir = this.OUTSelect(iPacX, iPacY, iPacDir);
					break;
				case BLIND:
					this.iDir = this.BLINDSelect(iPacX, iPacY, iPacDir);
					break;
				case EYE:
					this.iDir = this.EYESelect();
			}

		if (this.iStatus != this.EYE) {
			this.iX += MappingHelper.iXDirection[this.iDir];
			this.iY += MappingHelper.iYDirection[this.iDir];
		} else {
			this.iX += 2 * MappingHelper.iXDirection[this.iDir];
			this.iY += 2 * MappingHelper.iYDirection[this.iDir];
		}

	}

	public int INSelect()
			// count available directions
			throws Error {
		int iM, i, iRand;
		int iDirTotal = 0;

		for (i = 0; i < 4; i++) {
			iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
			if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir])
				iDirTotal++;
		}
		// randomly select a direction
		if (iDirTotal != 0) {
			iRand = Helper.RandSelect(iDirTotal);
			if (iRand >= iDirTotal)
				throw new Error("iRand out of range");
			// exit(2);
			for (i = 0; i < 4; i++) {
				iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
				if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir]) {
					iRand--;
					if (iRand < 0)
					// the right selection
					{
						if (iM == TileentityMaze.DOOR)
							this.iStatus = this.OUT;
						this.iDir = i;
						break;
					}
				}
			}
		}
		return (this.iDir);
	}

	public int OUTSelect(int iPacX, int iPacY, int iPacDir)
			// count available directions
			throws Error {
		int iM, i, iRand;
		int iDirTotal = 0;
		int[] iDirCount = new int[4];

		for (i = 0; i < 4; i++) {
			iDirCount[i] = 0;
			iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
			if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir] && iM != TileentityMaze.DOOR)
			// door is not accessible for OUT
			{
				iDirCount[i]++;
				iDirCount[i] += this.iDir == iPacDir ? this.DIR_FACTOR : 0;
				switch (i) {
					case 0: // right
						iDirCount[i] += iPacX > this.iX ? this.POS_FACTOR : 0;
						break;
					case 1: // up
						iDirCount[i] += iPacY < this.iY ? this.POS_FACTOR : 0;
						break;
					case 2: // left
						iDirCount[i] += iPacX < this.iX ? this.POS_FACTOR : 0;
						break;
					case 3: // down
						iDirCount[i] += iPacY > this.iY ? this.POS_FACTOR : 0;
						break;
				}
				iDirTotal += iDirCount[i];
			}
		}
		// randomly select a direction
		if (iDirTotal != 0) {
			iRand = Helper.RandSelect(iDirTotal);
			if (iRand >= iDirTotal)
				throw new Error("iRand out of range");
			// exit(2);
			for (i = 0; i < 4; i++) {
				iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
				if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir] && iM != TileentityMaze.DOOR) {
					iRand -= iDirCount[i];
					if (iRand < 0)
					// the right selection
					{
						this.iDir = i;
						break;
					}
				}
			}
		} else
			throw new Error("iDirTotal out of range");
		// exit(1);
		return (this.iDir);
	}

	public void blind() {
		if (this.iStatus == this.BLIND || this.iStatus == this.OUT) {
			this.iStatus = this.BLIND;
			this.iBlindCount = this.blindCount;
			this.iBlink = 0;
			// reverse
			if (this.iX % 16 != 0 || this.iY % 16 != 0) {
				this.iDir = MappingHelper.iBack[this.iDir];
				// a special condition:
				// when ghost is leaving home, it can not go back
				// while becoming blind
				int iM;
				iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[this.iDir]][this.iX / 16 + MappingHelper.iXDirection[this.iDir]];
				if (iM == TileentityMaze.DOOR)
					this.iDir = MappingHelper.iBack[this.iDir];
			}
		}
	}

	public int EYESelect()
			// count available directions
			throws Error {
		int iM, i, iRand;
		int iDirTotal = 0;
		int[] iDirCount = new int[4];

		for (i = 0; i < 4; i++) {
			iDirCount[i] = 0;
			iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
			if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir]) {
				iDirCount[i]++;
				switch (i) {
					// door position 10,6
					case 0: // right
						iDirCount[i] += 160 > this.iX ? this.POS_FACTOR : 0;
						break;
					case 1: // up
						iDirCount[i] += 96 < this.iY ? this.POS_FACTOR : 0;
						break;
					case 2: // left
						iDirCount[i] += 160 < this.iX ? this.POS_FACTOR : 0;
						break;
					case 3: // down
						iDirCount[i] += 96 > this.iY ? this.POS_FACTOR : 0;
						break;
				}
				iDirTotal += iDirCount[i];
			}
		}
		// randomly select a direction
		if (iDirTotal != 0) {
			iRand = Helper.RandSelect(iDirTotal);
			if (iRand >= iDirTotal)
				throw new Error("RandSelect out of range");
			// exit(2);
			for (i = 0; i < 4; i++) {
				iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
				if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir]) {
					iRand -= iDirCount[i];
					if (iRand < 0)
					// the right selection
					{
						if (iM == TileentityMaze.DOOR)
							this.iStatus = this.IN;
						this.iDir = i;
						break;
					}
				}
			}
		} else
			throw new Error("iDirTotal out of range");
		return (this.iDir);
	}

	public int BLINDSelect(int iPacX, int iPacY, int iPacDir)
			// count available directions
			throws Error {
		int iM, i, iRand;
		int iDirTotal = 0;
		int[] iDirCount = new int[4];

		for (i = 0; i < 4; i++) {
			iDirCount[i] = 0;
			iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
			if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir] && iM != TileentityMaze.DOOR)
			// door is not accessible for OUT
			{
				iDirCount[i]++;
				iDirCount[i] += this.iDir == iPacDir ? this.DIR_FACTOR : 0;
				switch (i) {
					case 0: // right
						iDirCount[i] += iPacX < this.iX ? this.POS_FACTOR : 0;
						break;
					case 1: // up
						iDirCount[i] += iPacY > this.iY ? this.POS_FACTOR : 0;
						break;
					case 2: // left
						iDirCount[i] += iPacX > this.iX ? this.POS_FACTOR : 0;
						break;
					case 3: // down
						iDirCount[i] += iPacY < this.iY ? this.POS_FACTOR : 0;
						break;
				}
				iDirTotal += iDirCount[i];
			}
		}
		// randomly select a direction
		if (iDirTotal != 0) {
			iRand = Helper.RandSelect(iDirTotal);
			if (iRand >= iDirTotal)
				throw new Error("RandSelect out of range");
			// exit(2);
			for (i = 0; i < 4; i++) {
				iM = this.maze.iMaze[this.iY / 16 + MappingHelper.iYDirection[i]][this.iX / 16 + MappingHelper.iXDirection[i]];
				if (iM != TileentityMaze.WALL && i != MappingHelper.iBack[this.iDir]) {
					iRand -= iDirCount[i];
					if (iRand < 0)
					// the right selection
					{
						this.iDir = i;
						break;
					}
				}
			}
		} else
			throw new Error("iDirTotal out of range");
		return (this.iDir);
	}

	// return 1 if caught the pac!
	// return 2 if being caught by pac
	public int testCollision(int iPacX, int iPacY) {
		if (this.iX <= iPacX + 2 && this.iX >= iPacX - 2 && this.iY <= iPacY + 2 && this.iY >= iPacY - 2)
			switch (this.iStatus) {
				case OUT:
					return (1);
				case BLIND:
					this.iStatus = this.EYE;
					this.iX = this.iX / 4 * 4;
					this.iY = this.iY / 4 * 4;
					return (2);
			}
		// nothing
		return (0);
	}
	// (c) 2016 Joshua Sonnet
}
