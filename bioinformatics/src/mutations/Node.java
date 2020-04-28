package mutations;

import java.util.ArrayList;
import java.util.HashMap;

public class Node<L, N extends Node<L, N>> {
	private final int id;
	private final HashMap<L, N> paths;
	
	public Node(int id) {
		this.id = id;
		paths = new HashMap<L, N>();
	}
	
	public void addPath(N node, L label) {paths.putIfAbsent(label, node);}
	
	public boolean hasPath(L label) {return paths.containsKey(label);}
	
	public N pathEnd(L label) {return paths.get(label);}
	
	public ArrayList<L> pathLabels() {return new ArrayList<L>(paths.keySet());}
	
	public ArrayList<N> pathEnds() {return new ArrayList<N>(paths.values());}
	
	public void replacePath(L oldLabel, L newLabel) {paths.put(newLabel, paths.remove(oldLabel));}
	
	public HashMap<L, N> getPaths() {return paths;}
	
	public int numPaths() {return paths.size();}
	
	public int getID() {return id;}
	
	public String toString() {
		String ret = "#" + id + ", paths out=";
		for (L label : pathLabels()) ret += "(" + label + "=" + pathEnd(label).getID() + ")";
		return ret;
	}
}