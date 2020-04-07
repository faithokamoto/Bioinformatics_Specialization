package phylogeny;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class ParsimonyTreeBuilderRunner {
	public static HashMap<Integer, ArrayList<Integer>> readAdjList(String filename, HashMap<Integer, char[]> labelMap) {
		HashMap<Integer, ArrayList<Integer>> adjList = new HashMap<Integer, ArrayList<Integer>>();
		try {
			Scanner reader = new Scanner(new File(filename));
			int n = reader.nextInt();
			reader.nextLine();
			int curLeaf = 0;
			while (reader.hasNextLine()) {
				String[] line = reader.nextLine().split("->");
				if (curLeaf % 2 == 1 || curLeaf >= n * 2)
					adjList.putIfAbsent(Integer.parseInt(line[0]), new ArrayList<Integer>());
				if (curLeaf < n * 2 && curLeaf % 2 == 1) {
					adjList.get(Integer.parseInt(line[0])).add(curLeaf / 2);
					labelMap.put(curLeaf / 2, line[1].toCharArray());
				}
				else if (curLeaf >= n * 2)
					adjList.get(Integer.parseInt(line[0])).add(Integer.parseInt(line[1]));
				curLeaf++;
			}
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return adjList;
	}
	public static void main(String[] args) {
		HashMap<Integer, char[]> labelMap = new HashMap<Integer, char[]>();
		HashMap<Integer, ArrayList<Integer>> adjList = readAdjList("src/phylogeny/data.txt", labelMap);
		UnrootedParsimonyTreeBuilder run = new UnrootedParsimonyTreeBuilder(adjList, labelMap);
		run.buildTree();
		run.writeAdjList("src/phylogeny/output.txt");
	}
}