package clusters;

import phylogeny.DistanceMatrixUPGMA;

import java.util.HashMap;
import java.util.ArrayList;

public class DistanceMatrixTracker extends DistanceMatrixUPGMA {
	HashMap<Integer, ArrayList<Integer>> nodesIn;
	
	public DistanceMatrixTracker(double[][] arrMatrix) {
		super(arrMatrix);
		nodesIn = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < arrMatrix.length; i++) {
			nodesIn.put(i, new ArrayList<Integer>());
			nodesIn.get(i).add(i);
		}
	}

	public void merge(int one, int two) {
		super.merge(one, two);
		nodesIn.put(getMaxNode(), new ArrayList<Integer>());
		nodesIn.get(getMaxNode()).addAll(nodesIn.remove(one));
		nodesIn.get(getMaxNode()).addAll(nodesIn.remove(two));
		for (Integer node : nodesIn.get(getMaxNode())) {
			System.out.print((node + 1) + " ");
		}
		System.out.println();
	}
}