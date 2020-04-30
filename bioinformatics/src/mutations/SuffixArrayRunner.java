package mutations;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class SuffixArrayRunner {
	public static void main(String[] args) throws IOException {
		Scanner reader = new Scanner(new File("src/mutations/data.txt"));
		System.out.println(SuffixArray.countMatches(reader.next(), reader.next()));
		//int[] arr = SuffixArray.countMatches(reader.next(), new String[] {reader.next(), reader.next()});
		reader.close();
		//System.out.print(arr[0] + " " + arr[1]);
	}
}