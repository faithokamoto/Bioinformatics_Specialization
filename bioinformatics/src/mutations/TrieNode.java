package mutations;

public class TrieNode extends Node<Character, TrieNode>{
	private boolean isEnd;
	
	public TrieNode(int id) {
		super(id);
		isEnd = false;
	}
	
	public void makeEnd() {isEnd = true;}
	
	public boolean isEnd() {return isEnd;}
}