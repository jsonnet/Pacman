package utilities;

/**
 * provide some global public utility functions
 */
public class Helper {
	public static int RandDo(int iOdds)
	// see if it happens within a probability of 1/odds
	{
		if (Math.random() * iOdds < 1)
			return (1);
		return (0);
	}

	// return a random number within [0..iTotal)
	public static int RandSelect(int iTotal) {
		double a;
		a = Math.random();
		a = a * iTotal;
		return ((int) a);
	}

	public static int IntSign(int iD) {
		if (iD == 0)
			return (0);
		if (iD > 0)
			return (1);
		return (-1);
	}
	// (c) 2016 Joshua Sonnet
}
