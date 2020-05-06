package mutations;

import java.util.Scanner;
import java.io.File;

public class HMMRunner {
	public static char[] outcome, alph, states;
	public static double[][] trans, emiss;
	
	public static void readData(String filename) {
		try {
			Scanner reader = new Scanner(new File(filename));
			outcome = reader.nextLine().toCharArray();
			reader.nextLine();
			String[] alphStr = reader.nextLine().split(" ");
			alph = new char[alphStr.length];
			for (int i = 0; i < alph.length; ++i) alph[i] = alphStr[i].charAt(0);
			reader.nextLine();
			String[] statesStr = reader.nextLine().split(" ");
			states = new char[statesStr.length];
			for (int i = 0; i < states.length; ++i) states[i] = statesStr[i].charAt(0);
			reader.nextLine();
			reader.nextLine();
			trans = new double[states.length][states.length];
			for (int i = 0; i < states.length; ++i) {
				reader.next();
				for (int j = 0; j < states.length; ++j) {
					trans[i][j] = reader.nextDouble();
				}
			}
			reader.nextLine();
			reader.nextLine();
			reader.nextLine();
			emiss = new double[states.length][alph.length];
			for (int i = 0; i < states.length; ++i) {
				reader.next();
				for (int j = 0; j < alph.length; ++j) {
					emiss[i][j] = reader.nextDouble();
				}
			}
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		readData("src/mutations/data.txt");
		HMM run = new HMM(alph, LabeledChar.convert(states), trans, emiss);
		System.out.println(run);
	}
}