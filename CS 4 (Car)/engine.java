import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JFrame;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.util.ArrayList;
import java.awt.Color;

import java.awt.PointerInfo;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Point;
import java.util.Stack;
import sun.audio.*;
import java.awt.Robot;
public class engine extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private boolean running;
	private BufferedImage image;
	public int[] pixels;
	public key_listener camera;
	public screen screen;
	static boolean mouse_trap=true;
	static Point trap;
	static long time;
	static double counter;
	static int xrot_inc,yrot_inc;
	static int x_inc,y_inc,z_inc;
	static boolean firstTime=true;
	static int side_inc;
	public engine() {
		thread = new Thread(this);
		image = new BufferedImage(640, 640, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		camera = new key_listener();
		screen = new screen();
		
		addMouseMotionListener(camera);
		addMouseListener(camera);
		addKeyListener(camera);
		setSize(640, 640);
		setResizable(false);
		setTitle("3D Engine");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true);
		trap=getLocation();
		trap.setLocation(trap.getX()+320,trap.getY()+320);
		start();
	}
	private synchronized void start() {
		running = true;
		thread.start();
	}
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		bs.show();
	}
	public void run() {
		requestFocus();
		// double ns=1000000000.0 / 60.0;
		// double res=0;
		// float smooth=0.75f;
		// long temptime=0;
		// int temp=0;
		// double delta=0;
		// long lastTime=System.nanoTime();
		while(running) {
			// System.out.println(mouse_trap);
			long now = System.nanoTime();
			if(mouse_trap)
			{
				try
				{
					Robot a=new Robot();
					a.mouseMove((int)trap.getX(),(int)trap.getY());
				}
				catch(Exception ex)
				{
				}
			}
			//System.out.println(new Color(255,255,255).getRGB());
//			System.out.println(delta);
			//handles all of the logic restricted time
			// while (delta >= 1)
			// {
	//			System.out.println("outside:"+Math.sin(screen.xrot)+" "+Math.cos(screen.xrot)+" "+Math.sin(screen.yrot)+" "+Math.cos(screen.yrot)+" "+Math.sin(screen.zrot)+" "+Math.cos(screen.zrot));
				// delta--;
				// temp++;
			// }
//			update_screen=true;
			screen.xrot+=xrot_inc;
			screen.yrot=(screen.yrot+yrot_inc+2880)%360;
			if(xrot_inc!=0||yrot_inc!=0||firstTime)
			{
				screen.cosYrot=Math.cos(-screen.yrot*Math.PI/180);
				screen.sinYrot=Math.sin(-screen.yrot*Math.PI/180);
				screen.cosXrot=Math.cos(-screen.xrot*Math.PI/180);
				screen.sinXrot=Math.sin(-screen.xrot*Math.PI/180);
				screen.sinXrotCosYrot=Math.sin(-screen.xrot*Math.PI/180)*Math.cos(-screen.yrot*Math.PI/180);
				screen.sinXrotSinYrot=Math.sin(-screen.xrot*Math.PI/180)*Math.sin(-screen.yrot*Math.PI/180);
				screen.cosXrotCosYrot=Math.cos(-screen.xrot*Math.PI/180)*Math.cos(-screen.yrot*Math.PI/180);
				screen.cosXrotSinYrot=Math.cos(-screen.xrot*Math.PI/180)*Math.sin(-screen.yrot*Math.PI/180);
				firstTime=false;
			}
			xrot_inc=0;
			yrot_inc=0;
			if(screen.xrot>270)
				screen.xrot=270;
			else if(screen.xrot<90)
				screen.xrot=90;
			screen.camera_x+=x_inc*Math.sin(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			screen.camera_z+=y_inc*Math.cos(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			screen.camera_y+=z_inc*Math.sin((screen.xrot-180)*Math.PI/180)/10;
			screen.camera_x-=side_inc*Math.sin((screen.yrot-90)*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			screen.camera_z+=side_inc*Math.cos((screen.yrot-90)*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;

			side_inc=0;
			x_inc=0;
			y_inc=0;
			z_inc=0;
			long time=System.nanoTime();
			screen.update(pixels);
			System.out.println("time:"+(System.nanoTime()-time));
//			update_screen=false;
			//delta--;
			// System.out.println(delta);
			// System.out.println(System.nanoTime()-now);
			// System.out.println(System.nanoTime()-now);
			render();//displays to the screen unrestricted time
//			System.out.println(blocks.get(9).pixels[0]);
		}
	}
	public static void main(String[] args) {
		engine game = new engine();
	}
}