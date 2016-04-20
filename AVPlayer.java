
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class AVPlayer implements Runnable{

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
	private final int fps = 24;
	long sec_pre_frame = (long)1000.0f/fps;

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
				lbIm1.setIcon(new ImageIcon(img));

				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				long end = System.currentTimeMillis();
				long difference = start - end;
				if(difference < sec_pre_frame)
					Thread.sleep(sec_pre_frame - difference);


			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		s.checkSync();
		System.out.println("vedio one sec");
		return true;
	}

	@Override
	public void run(){
		try {
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

		AVPlayer ren = new AVPlayer();
		ren.initialize(args, s);
		Thread playvideo = new Thread(ren);

		PlaySound ps = new PlaySound(args[1], s);
		Thread playsound = new Thread(ps);
		
		playvideo.start();
		playsound.start();
	}

}