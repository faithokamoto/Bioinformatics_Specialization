package phylogeny;


public class TreeRunner{
	public static void main(String[] args) {
		Tree run = new Tree("src/phylogeny/data.txt");
		
		int[][] distMatrix = run.getDistanceMatrix();
		for (int i = 0, n = run.getN(); i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(distMatrix[i][j]);
				if (j != n - 1) System.out.print("\t");
			}
			System.out.println();
		}
	}
}
