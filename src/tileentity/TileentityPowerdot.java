package tileentity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import entity.EntityGhost;
import utilities.AssetLoader;

public class TileentityPowerdot {
	final int iX[] = { 1, 19, 1, 19 };
	final int iY[] = { 2, 2, 13, 13 };

	final int iShowCount = 32;
	final int iHideCount = 16;

	int frameCount;
	int showStatus;

	int iValid[];

	// the applet this object is associated to
	Window applet;
	Graphics graphics;

	// the ghosts it controls
	EntityGhost[] ghosts;

	// the power dot image
	Image imagePowerDot;

	// the blank image
	public Image imageBlank;

	public TileentityPowerdot(Window a, Graphics g, EntityGhost[] gh) {
		this.applet = a;
		this.graphics = g;
		this.ghosts = gh;

		// initialize power dot and image
		this.iValid = new int[4];

		this.imagePowerDot = this.applet.createImage(16, 16);
		AssetLoader.drawPowerDot(this.imagePowerDot);

		this.imageBlank = this.applet.createImage(16, 16);
		Graphics imageG = this.imageBlank.getGraphics();
		imageG.setColor(Color.black);
		imageG.fillRect(0, 0, 16, 16);

		this.frameCount = this.iShowCount;
		this.showStatus = 1; // show
	}

	public void start() {
		// all power dots available
		for (int i = 0; i < 4; i++)
			this.iValid[i] = 1;
	}

	void clear(int dot) {
		this.graphics.drawImage(this.imageBlank, this.iX[dot] * 16, this.iY[dot] * 16, this.applet);
	}

	public void eat(int iCol, int iRow) {
		for (int i = 0; i < 4; i++)
			if (this.iX[i] == iCol && this.iY[i] == iRow) {
				this.iValid[i] = 0;
				this.clear(i);
			}
		for (int i = 0; i < 4; i++)
			this.ghosts[i].blind();
	}

	public void draw() {
		this.frameCount--;
		if (this.frameCount == 0)
			if (this.showStatus == 1) {
				this.showStatus = 0;
				this.frameCount = this.iHideCount;
			} else {
				this.showStatus = 1;
				this.frameCount = this.iShowCount;
			}
		for (int i = 0; i < 4; i++)
			if (this.iValid[i] == 1 && this.showStatus == 1)
				this.graphics.drawImage(this.imagePowerDot, this.iX[i] * 16, this.iY[i] * 16, this.applet);
			else
				this.graphics.drawImage(this.imageBlank, this.iX[i] * 16, this.iY[i] * 16, this.applet);
	}
	// (c) 2016 Joshua Sonnet
}
