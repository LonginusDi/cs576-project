
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.awt.event.ActionEvent;

public class AVPlayer implements Runnable{

	static JFrame frame;
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
	int reversebit = 0;

	int framecount = 0;
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
		} catch (IOException e) {
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


				// 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

   	// 	 Action actionListener = new AbstractAction() {
    //   		public void actionPerformed(ActionEvent actionEvent) {
    //     		//System.out.println("Got an M");
    //     		reverse();
    //   		}
    // 	};

    // JPanel content = (JPanel) frame.getContentPane();
    // KeyStroke stroke = KeyStroke.getKeyStroke("M");

    // InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    // inputMap.put(stroke, "OPEN");
    // content.getActionMap().put("OPEN", actionListener);
		
		
	}

	public void reverse(){
		if (reversebit == 0) {
			s.pause();
		} else {
			s.resume();
		}
		reversebit = 1- reversebit;
	

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
				framecount++;
				System.out.println(framecount);
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				long end = System.currentTimeMillis();
				long difference = start - end;
				if(difference < sec_pre_frame)
					Thread.sleep(sec_pre_frame - difference);
				s.look();

			}
		}
		catch (Exception e)
		{
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

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
			return;
		}
		sync s = new sync();

		final AVPlayer ren = new AVPlayer();
		ren.initialize(args, s);
		final Thread playvideo = new Thread(ren);

		final PlaySound ps = new PlaySound(args[1], s);
		Thread playsound = new Thread(ps);
		
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

   		 Action actionListener = new AbstractAction() {
      		public void actionPerformed(ActionEvent actionEvent) {
        		//System.out.println("dsfdsf");
        		ren.reverse();
      		}
    	};

    JPanel content = (JPanel) frame.getContentPane();
    KeyStroke stroke = KeyStroke.getKeyStroke("P");

    InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(stroke, "OPEN");
    content.getActionMap().put("OPEN", actionListener);


		BreakDown test = new BreakDown(args[0], args[1], args[2]);
		test.initialize();
	

		frameAt = test.selectedFrame;
		sectionList = test.sectionList;
		
		int index = -1;
		for (int i = 0; i<sectionList.size(); i++) {
			start = (int)sectionList.get(i).startingFrame;
			end = (int)sectionList.get(i).endingFrame;
			if( start <= frameAt &&  end >= frameAt) {
				index = i;
				break;
			}

		}

		if (frameAt - start < 30) {
			int length = frameAt-start;
			int preIndex = index;
			while(preIndex >=0 && length < 30) {
				
				preIndex --;
				start = (int)sectionList.get(preIndex).startingFrame;
				length += (int)sectionList.get(preIndex).endingFrame - (int)sectionList.get(preIndex).startingFrame;
			}
		}

		if (end - frameAt < 30) {
			int length = end-frameAt;
			int preIndex = index;
			while(preIndex < sectionList.size() && length < 30) {
				
				preIndex ++;
				end = (int)sectionList.get(preIndex).endingFrame;
				
				length += (int)sectionList.get(preIndex).endingFrame - (int)sectionList.get(preIndex).startingFrame;
			}
		}

		ps.setPos(start,end);
       ren.reverse();

		playvideo.start();
		playsound.start();

	}

}