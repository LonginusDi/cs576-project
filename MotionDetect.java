import java.util.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MotionDetect {

	int width = 480;
	int height = 270;

	int frame = 0;
	double min = Double.MAX_VALUE;

	// get motion vector for the entire frame
	public ArrayList<ArrayList<MotionVector>> getkMotionVector(byte[] previous, byte[] current, byte[] indexpic){
		double[][] preImg = new double[height][width];
		double[][] curImg = new double[height][width];
		double[][] indImg = new double[height][width];
		ArrayList<MotionVector> motionVector = new ArrayList<MotionVector>();
		ArrayList<MotionVector> indexVector = new ArrayList<MotionVector>();

		preImg = convertMatrix(previous);
		curImg = convertMatrix(current);
		indImg = convertMatrix(indexpic);

		// find vector for each 16*16 block
		for(int i = 0; i<= height - 16; i += 16){
			for(int j = 0; j <= width - 16; j += 16){
				// System.out.println("y: "+ i);
				// System.out.println("x: "+ j);
				// find the range to compare
				int left = (j - 16 > 0) ? j-16 : 0;
				int right = (j + 32 < width) ? j + 32 : width-1;
				int bot = (i - 16 > 0) ? i - 16 : 0;
				int top = (i + 32 < height) ? i + 32 : height-1;
				double lowestScore = Double.MAX_VALUE;
				double xVec = Double.MAX_VALUE;
				double yVec = Double.MAX_VALUE;

				double indlowestScore = Double.MAX_VALUE;
				double indxVec = Double.MAX_VALUE;
				double indyVec = Double.MAX_VALUE;

				for(int y = bot; y < top - 16; y++){
					for(int x = left; x < right - 16; x++){
						// use MAD to find difference between two blocks
						double mad = 0;
						double indmad = 0;
						for(int q = 0; q < 16; q++){
							for(int p = 0; p < 16; p++){
								double r = Math.abs(curImg[q + i][p + j] - preImg[q + y][p + x]);
								double g = Math.abs(indImg[q + i][p + j] - preImg[q + y][p + x]);
								// double b = Math.abs(curImg[q + i][p + j][2] - preImg[q + y][p + x][2]);
								mad += r;//Math.sqrt(r*r + g*g + b*b);
								indmad += g;
							}
						}
						if(indmad < indlowestScore){
								indlowestScore = indmad;
								indxVec = x - j;
								indyVec = y - i;
							}

							if(mad < lowestScore){
								lowestScore = mad;
								xVec = x - j;
								yVec = y - i;
							}

					}
				}
				//System.out.println("low: "+lowestScore);

				MotionVector motion = new MotionVector(j, i, xVec, yVec, lowestScore);
				motionVector.add(motion);

				MotionVector m = new MotionVector(j, i, indxVec, indyVec, indlowestScore);
				indexVector.add(m);

			}
		}

		ArrayList<ArrayList<MotionVector>> result = new ArrayList<ArrayList<MotionVector>>();
		result.add(motionVector);
		result.add(indexVector);

		return result;
	}

	//check with threshold
	public boolean checkMotion(byte[] previous, byte[] current, byte[] indexpic, double motionThres, double colorThres, int framecount){
		ArrayList<ArrayList<MotionVector>> motionVector = getkMotionVector(previous, current, indexpic);
		double avgMotion = 0;
		double colorDiff = 0;
		double indavgMotion = 0;
		double indcolorDiff = 0;
		

		for(MotionVector m: motionVector.get(0)){
			// add up motion vectors and collor difference
			avgMotion += Math.sqrt(m.xVector * m.xVector + m.yVector * m.yVector);
			colorDiff += m.madScore;
		}

		for(MotionVector m: motionVector.get(1)){
			// add up motion vectors and collor difference
			indavgMotion += Math.sqrt(m.xVector * m.xVector + m.yVector * m.yVector);
			indcolorDiff += m.madScore;
		}

		// average motion vector
		avgMotion /= motionVector.get(0).size();
		colorDiff /= motionVector.get(0).size();
		// System.out.println("motion vector: "+avgMotion);
		// System.out.println("colordiff: "+Math.abs(colorDiff));

		indavgMotion /= motionVector.get(1).size();
		indcolorDiff /= motionVector.get(1).size();

		if(indavgMotion < min){
			min = indavgMotion;
			frame = framecount;
		}


		System.out.println("motion vector: "+indavgMotion);
		System.out.println("colordiff: "+Math.abs(indcolorDiff));


		// check with threshold
		if(avgMotion > motionThres || colorDiff > colorThres)
			return true;

		return false;
	}


	public double[][] convertMatrix(byte[] bytes){
		double[][] temp = new double[height][width];
				int ind = 0;
				for(int y = 0; y < height; y++){

					for(int x = 0; x < width; x++){

						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 

						short sr = (short)(r & 0xff);
						short gr = (short)(g & 0xff);
						short br = (short)(b & 0xff);

						int rr = sr;
						int gg = gr ;
						int bb = br;

						temp[y][x] = (rr/1.000) * 0.30 + (gg/1.000) * 0.59 + (bb/1.000) * 0.11;
						ind++;
					}
				}
		return temp;
	}

		//cover from [] to [][][]
	// 	public double[][][] convertMatrix(byte[] bytes){
	// 	double[][][] temp = new double[height][width][3];
	// 			int ind = 0;
	// 			for(int y = 0; y < height; y++){

	// 				for(int x = 0; x < width; x++){

	// 					byte r = bytes[ind];
	// 					byte g = bytes[ind+height*width];
	// 					byte b = bytes[ind+height*width*2]; 

	// 					short sr = (short)(r & 0xff);
	// 					short gr = (short)(g & 0xff);
	// 					short br = (short)(b & 0xff);

	// 					int rr = sr;
	// 					int gg = gr ;
	// 					int bb = br;

	// 					//temp[y][x] = (rr/1.000) * 0.30 + (gg/1.000) * 0.59 + (bb/1.000) * 0.11;
	// 					temp[y][x][0] = rr/1.000;
	// 					temp[y][x][1] = rr/1.000;
	// 					temp[y][x][2] = rr/1.000;
	// 					ind++;
	// 				}
	// 			}
	// 	return temp;
	// }
}