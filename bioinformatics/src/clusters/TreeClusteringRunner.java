package clusters;

// for readDistMatrix
import phylogeny.TreeBuilderRunner;

/**
 * Simple runner for the TreeClustering class
 * @author faith
 */
public class TreeClusteringRunner extends TreeBuilderRunner {
	public static void main(String[] args) {
		TreeClustering run = new TreeClustering(readDistMatrix("src/clusters/data.txt"));
		run.buildTree();
	}
}