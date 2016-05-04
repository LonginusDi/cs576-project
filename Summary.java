
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Summary implements Runnable{

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	GridBagConstraints c;
	BufferedImage img;
	sync s;

	int width = 480;
	int height = 270;
	long len = width*height*3;
	byte[] bytes = new byte[(int)len];
	InputStream is;
	private final int fps = 15;
	long sec_pre_frame = (long)600.0f/fps;

	int framecount = 0;
	int sframe = 0;
	// int[] breakFrame = {1, 31, 119, 132, 134, 136, 174, 190, 192, 245, 247, 254, 256, 268, 278, 280, 451, 453, 455, 477, 531, 533, 556, 664, 685, 687, 698, 720, 751, 753, 755, 775, 795, 809, 819, 826, 848, 850, 852, 864, 866, 876, 878, 880, 887, 893, 895, 901, 903, 905, 907, 909, 913, 935, 940, 942, 974, 976, 978, 1055, 1057, 1067, 1100, 1102, 1106, 1149, 1151, 1153, 1155, 1157, 1159, 1166, 1168, 1170, 1172, 1174, 1200, 1262, 1264, 1266, 1282, 1284, 1286, 1291, 1293, 1295, 1312, 1314, 1319, 1321, 1334, 1336, 1338, 1345, 1347, 1349, 1351, 1359, 1361, 1363, 1365, 1370, 1372, 1374, 1376, 1378, 1380, 1382, 1389, 1391, 1395, 1397, 1400, 1402, 1404, 1420, 1449, 1476, 1478, 1485, 1487, 1496, 1498, 1502, 1572, 1574, 1581, 1583, 1751, 1826, 1845, 1847, 1855, 1858, 1882, 1887, 1955, 1957, 2043, 2045, 2052, 2054, 2123, 2287, 2289, 2291, 2324, 2405, 2407, 2409, 2477, 2479, 2481, 2497, 2499, 2510, 2512, 2514, 2522, 2524, 2526, 2539, 2573, 2727, 2729, 2750, 2815, 2818, 2820, 2847, 3099, 3101, 3103, 3105, 3127, 3129, 3133, 3156, 3158, 3166, 3186, 3190, 3203, 3205, 3256, 3259, 3261, 3267, 3269, 3300, 3484, 3526, 3606, 3608, 3756, 3812, 3839, 3848, 3853, 3856, 3860, 3864, 3867, 3963, 3965, 3970, 3972, 3974, 3977, 4017, 4019, 4021, 4095, 4097, 4099, 4101, 4137, 4193, 4195, 4229, 4231, 4233, 4235, 4239, 4257, 4259, 4261, 4283, 4285, 4287, 4316, 4324, 4326, 4328, 4330, 4332, 4408, 4410, 4412, 4414};
	static ArrayList<Integer> breakFrame = new ArrayList<Integer>();

	static int lowerBound = 62;
	static int upperBound = 150;

	static int frameAt;
	static ArrayList<Section> sectionList = new ArrayList<Section>();
	static int start = 1;
	static int end = 4500;

	public void initialize(String[] args, sync sy){

		try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			this.s = sy;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Video: " + args[0]);
		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel lbText2 = new JLabel("Audio: " + args[1]);
		lbText2.setHorizontalAlignment(SwingConstants.LEFT);
		lbIm1 = new JLabel(new ImageIcon(img));

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
		
		
	}
	
	public boolean oneSecond(){
		try{
			for(int i = 0; i < fps; i++){
				long start = System.currentTimeMillis();
				int ind = 0;
				for(int y = 0; y < height; y++){

					for(int x = 0; x < width; x++){

						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 

						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
			//if(framecount  >= 250 && framecount<=450)
				if(framecount > end)
					return false;

				lbIm1.setIcon(new ImageIcon(img));

				// if(framecount == breakFrame[sframe]){
				// 	is.skip(100*len); /// 1500 -2000 break;
				// 	framecount += 50;
				// 	System.out.println(breakFrame[sframe] + "Break!!!!!!!!!!!!!!!");
				// 	sframe++;
				// }else{
				// 	framecount++;
				// 	System.out.println(framecount);

				// }
				
				//KKKK
				//Skip frame difference for certain length, 
				int diff = 0;
				if(sframe < breakFrame.size() - 1){
					diff = breakFrame.get(sframe + 1) - breakFrame.get(sframe);
				}

				if(sframe < breakFrame.size()){

					if(framecount == breakFrame.get(sframe)){
						if(diff > lowerBound && diff < upperBound){
							framecount++;
							System.out.println(breakFrame.get(sframe) + " Don't Break!!");
							System.out.println("Diff is " +  diff);

							int offset = 0;
							int numRead = 0;
							while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
								offset += numRead;
							}

						}else{
							is.skip(diff*len); /// 1500 -2000 break;
							framecount += diff;
							System.out.println(breakFrame.get(sframe) + " Break!!");
							System.out.println("Diff is " +  diff);
						}
							
						sframe++;

					}else{
						// if(diff > 45 && diff < 150){
						// 	framecount++;
						// 	System.out.println(framecount+" - "+breakFrame[sframe]);
						// }else{
						// 	is.skip(2*len); /// 1500 -2000 break;
						// 	framecount += 2;
						// 	System.out.println(breakFrame[sframe] + "Break!!!!");
						// 	System.out.println("Diff is " +  diff);
						// 	sframe++;
						// }
						framecount++;

						int offset = 0;
						int numRead = 0;
						while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
							offset += numRead;
						}

						System.out.print("+");
					}
				}

				// if(diff < 45 || diff > 150 ){
				// 	is.skip(diff*len); /// 1500 -2000 break;
				// 	framecount += diff;
				// 	System.out.println(breakFrame[sframe] + "Break!!!!");
				// 	sframe++;
				// }else{
				// 	framecount++;
				// 	System.out.print("+");
				// }			


				// if(framecount % 50 == 0){
				// 	is.skip(25*len); /// 1500 -2000 break;
				// 	framecount += 25;
				// 	System.out.println(breakFrame[sframe] + "Break!!!!");
				// 	int diff = breakFrame[sframe + 1] - breakFrame[sframe];
				// 	System.out.println("Diff is " +  diff);

				// 	sframe++;
				// }else{
				// 	framecount++;
				// 	System.out.print("+");

				// }


				// int offset = 0;
				// int numRead = 0;
				// while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				// 	offset += numRead;
				// }


				long end = System.currentTimeMillis();
				long difference = start - end;
				if(difference < sec_pre_frame)
					Thread.sleep(sec_pre_frame - difference);


			}
		}
		catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}

		s.checkSync();
		//System.out.println("vedio one sec");
		return true;
	}

	@Override
	public void run(){

		try {
			is.skip((start-1)*len); /// 1500 -2000 break;
			framecount = start;
		int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
			}
			catch (IOException e){}

			while(oneSecond()){}

	}

	public int totalFrame(int lowerBound, int UpperBound){
		int temp = 0;
		for(int i = 0; i < breakFrame.size() - 1; i++){
			int diff = breakFrame.get(i + 1) - breakFrame.get(i);
			//run.rgb, Alin_Day1_003, result_run
			//if(diff > 62 && diff < 150){ 
			//demo.rgb, Yin_Snack, result_demo
			//if(diff > 30 && diff < 190){

			//Alireza_Day2_003.rgb, result
			if(diff > lowerBound && diff < UpperBound){

				temp += diff;

			}
		}
		return temp;
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
			return;
		}

		breakFrame = new ArrayList<Integer>();
		try{
		    // Open the file that is the first
		    // command line parameter
		    FileInputStream fstream = new FileInputStream("result_run.txt");
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		      breakFrame.add(Integer.parseInt(strLine));
		    }
		    //Close the input stream
		    in.close();
		}catch (Exception e){ 

	      System.err.println("Error: " + e.getMessage()); 

		}


		// int temp = 0;
		// for(int i = 0; i < breakFrame.size() - 1; i++){
		// 	int diff = breakFrame.get(i + 1) - breakFrame.get(i);
		// 	//run.rgb, Alin_Day1_003, result_run
		// 	//if(diff > 62 && diff < 150){ 
		// 	//demo.rgb, Yin_Snack, result_demo
		// 	//if(diff > 30 && diff < 190){

		// 	//Alireza_Day2_003.rgb, result
		// 	if(diff > 62 && diff < 150){

		// 		temp += diff;

		// 	}
		// }

		Summary ren = new Summary();

		int temp = ren.totalFrame(lowerBound+1, upperBound+1);

		while(temp < 1000){
			lowerBound--;
			upperBound += 2;
			temp = ren.totalFrame(lowerBound, upperBound);
		}

		while(temp > 1250){
			lowerBound++;
			upperBound -= 2;
			temp = ren.totalFrame(lowerBound, upperBound);
		}

		System.out.println("Summary Total Frame: " + temp);

		System.out.println("LowerBound is " + lowerBound + ", UpperBound is " + upperBound);


		sync s = new sync();

		ren.initialize(args, s);
		Thread playvideo = new Thread(ren);

		SummarySound ps = new SummarySound(args[1], s, breakFrame, lowerBound, upperBound);
		Thread playsound = new Thread(ps);
		
		playvideo.start();
		playsound.start();
		

	}

}