package AI;

import entity.EntityGhost;
import entity.EntityPacman;
import tileentity.TileentityMaze;
import utilities.MappingHelper;

/**
 * calculate the move for pac
 * this is used for machine controlled pac
 * for demonstration or else...
 */
class DebugMoveIntelligence {
	// things that affect the decision:
	// ghosts status
	// ghosts position
	// ghosts movement direction
	// dots position
	// ONLY the closest dot is goal
	// powerdot position
	// the closer the ghosts, more weight to go to powerdot

	// direction score
	// each represents the score for that direction
	// the direction with the highest score will be chosen
	// if the chosen one is not available, the oposite score will be subtracted,
	// and the rest compared again.
	int iDirScore[];

	int iValid[];

	EntityPacman cPac;
	EntityGhost[] cGhost;
	TileentityMaze cMaze;

	DebugMoveIntelligence(EntityPacman pac, EntityGhost ghost[], TileentityMaze maze) {
		this.iDirScore = new int[4];

		this.iValid = new int[4];
		this.cPac = pac;

		this.cGhost = new EntityGhost[4];
		for (int i = 0; i < 4; i++)
			this.cGhost[i] = ghost[i];

		this.cMaze = maze;
	}

	public int GetNextDir() throws Error {
		int i;

		// first, init to 0
		for (i = 0; i < 4; i++)
			this.iDirScore[i] = 0;

		// add score for dot
		this.AddDotScore();

		// add score for ghosts
		this.AddGhostScore();

		// add score for powerdot
		this.AddPowerDotScore();

		// determine the direction based on scores

		for (i = 0; i < 4; i++)
			this.iValid[i] = 1;

		int iHigh, iHDir;

		while (true) {
			iHigh = -1000000;
			iHDir = -1;
			for (i = 0; i < 4; i++)
				if (this.iValid[i] == 1 && this.iDirScore[i] > iHigh) {
					iHDir = i;
					iHigh = this.iDirScore[i];
				}

			if (iHDir == -1)
				throw new Error("cpacmove: can't find a way?");

			if (this.cPac.getiX() % 16 == 0 && this.cPac.iY % 16 == 0) {
				if (this.cPac.mazeOK(this.cPac.getiX() / 16 + MappingHelper.iXDirection[iHDir], this.cPac.iY / 16 + MappingHelper.iYDirection[iHDir]))
					return (iHDir);
			} else
				return (iHDir);

			this.iValid[iHDir] = 0;
			// iDirScore[MappingHelper.iBack[iHDir]] = iDirScore[iHDir];

		}

		// return(iHDir); // will not reach here, ordered by javac
	}

	void AddGhostScore() {
		int iXDis, iYDis; // distance
		double iDis; // distance

		int iXFact, iYFact;

		// calculate ghosts one by one
		for (int i = 0; i < 4; i++) {
			iXDis = this.cGhost[i].iX - this.cPac.getiX();
			iYDis = this.cGhost[i].iY - this.cPac.iY;

			iDis = Math.sqrt(iXDis * iXDis + iYDis * iYDis);

			if (this.cGhost[i].iStatus == this.cGhost[i].BLIND) {

			} else {
				// adjust iDis into decision factor

				iDis = 18 * 13 / iDis / iDis;
				iXFact = (int) (iDis * iXDis);
				iYFact = (int) (iDis * iYDis);

				if (iXDis >= 0)
					this.iDirScore[MappingHelper.LEFT] += iXFact;
				else
					this.iDirScore[MappingHelper.RIGHT] += -iXFact;

				if (iYDis >= 0)
					this.iDirScore[MappingHelper.UP] += iYFact;
				else
					this.iDirScore[MappingHelper.DOWN] += -iYFact;
			}
		}
	}

	void AddDotScore() {
		int i, j;

		int dDis, dShortest;
		int iXDis, iYDis;
		int iX = 0, iY = 0;

		dShortest = 1000;

		// find the nearest dot
		for (i = 0; i < TileentityMaze.HEIGHT; i++)
			for (j = 0; j < TileentityMaze.WIDTH; j++)
				if (this.cMaze.iMaze[i][j] == TileentityMaze.DOT) {
					iXDis = j * 16 - 8 - this.cPac.getiX();
					iYDis = i * 16 - 8 - this.cPac.iY;
					dDis = iXDis * iXDis + iYDis * iYDis;

					if (dDis < dShortest) {
						dShortest = dDis;

						iX = iXDis;
						iY = iYDis;
					}
				}

		// now iX and iY is the goal (relative position)

		int iFact = 100000;

		if (iX > 0)
			this.iDirScore[MappingHelper.RIGHT] += iFact;
		else if (iX < 0)
			this.iDirScore[MappingHelper.LEFT] += iFact;

		if (iY > 0)
			this.iDirScore[MappingHelper.DOWN] += iFact;
		else if (iY < 0)
			this.iDirScore[MappingHelper.UP] += iFact;
	}

	void AddPowerDotScore() {

	}
}
