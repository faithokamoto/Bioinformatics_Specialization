package clusters;

// NOTE: no data to test this on

public class HalotypeSearch {
	private boolean[][] snps;
	
	@SuppressWarnings("unused")
	private HalotypeSearch() {}
	
	public HalotypeSearch(boolean[][] snps) {
		this.snps = snps;
	}
	
	private double score(int[] test, boolean[] explain) {
		double score = 0.0;
		int numDisT = 0;
		for (int i = 0; i < explain.length - 1; i++) {
			for (int j = i + 1; j < explain.length; j++) {
				if (explain[i] != explain[j]) {
					numDisT++;
					for (int snpNum : test) {
						if (snps[snpNum][i] != snps[snpNum][j]) {
							score++;
							break;
						}
					}
				}
			}
		}
		return score / numDisT;
	}
	
	private boolean arrEquals(int[] a, int[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) return false;
		}
		return true;
	}
	
	public int[] search(boolean[] explain, int num) {
		int[] best = new int[num];
		double maxScore = score(best, explain);
		for (int i = 0; i < num; i++) best[i] = i;
		while (true) {
			int[] cur = best.clone();
			for (int i = 0; i < cur.length; i++) {
				for (int j = 0; j < snps.length; j++) {
					if (cur[i] != j) {
						cur[i] = j;
						double curScore = score(cur, explain);
						if (curScore > maxScore) {
							best = cur.clone();
							maxScore = curScore;
						}
					}
				}
			}
			if (arrEquals(cur, best)) return best;
		}
	}
}