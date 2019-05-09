import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
class key_listener implements KeyListener,MouseListener,WindowFocusListener,WindowListener,MouseMotionListener{
	boolean pause_hold;
	static boolean change;
	static int limit;
	static boolean change1;
	public key_listener() {

	}
	public void windowGainedFocus(WindowEvent e)
	{

	}
	public void windowLostFocus(WindowEvent e)
	{

	}
	public void mouseClicked(MouseEvent mouse)
	{
		screen.destroy_area=true;		
	}
	public void mouseExited(MouseEvent mouse)
	{
	}
	public void mousePressed(MouseEvent mouse)
	{
	}
	public void mouseReleased(MouseEvent mouse)
	{
		
	}
	public void mouseEntered(MouseEvent mouse)
	{
	}
	public void windowOpened(WindowEvent e) {
		
	}
	public void windowClosing(WindowEvent e) {
		
	}
	public void windowClosed(WindowEvent e) {
		
	}
	public void windowIconified(WindowEvent e) {
		
	}
	public void windowDeiconified(WindowEvent e) {
		
	}
	public void windowActivated(WindowEvent e) {
		// engine.mouse_trap=true;
	}
	public void windowDeactivated(WindowEvent e) {
		// engine.mouse_trap=false;
	}
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode()==KeyEvent.VK_K)
		{
			screen.light.z1--;
		}
		if(key.getKeyCode()==KeyEvent.VK_L)
		{
			screen.light.z1++;
		}
		if(key.getKeyCode()==KeyEvent.VK_SPACE)
		{
			change=true;
		}
		if(key.getKeyCode()==KeyEvent.VK_B)
		{
			change1=true;
		}
		if(key.getKeyCode()==KeyEvent.VK_W)
		{
			// screen.camera_x+=(Math.cos(-(screen.yrot-360)*Math.PI/360)*0-Math.sin(-(screen.yrot-360)*Math.PI/360)*1)/10;
			// screen.camera_y-=(Math.sin(-(screen.xrot-360)*Math.PI/360)*(Math.cos(-(screen.yrot-360)*Math.PI/360)*1+Math.sin(-(screen.yrot-360)*Math.PI/360)*0)+Math.cos(-(screen.xrot-360)*Math.PI/360)*1)/10;
			// screen.camera_z+=(Math.cos(-(screen.xrot)*Math.PI/180)*(Math.cos(-(screen.yrot)*Math.PI/180)*1+Math.sin(-(screen.yrot)*Math.PI/180)*0)-Math.sin(-(screen.xrot)*Math.PI/180)*1)/10;
			engine.x_inc++;
			engine.y_inc--;
			engine.z_inc--;
			// screen.camera_x+=Math.sin(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_z-=Math.cos(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_y-=Math.sin((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_x-=Math.sin((screen.yrot-360)*Math.PI/360)/10;
			// screen.camera_y-=Math.sin((screen.xrot-360)*Math.PI/360)/10;
			// screen.camera_z+=Math.sin((screen.yrot-360)*Math.PI/360+Math.PI/2)/10;
			// System.out.println(screen.camera_z);
		}
		if(key.getKeyCode()==KeyEvent.VK_S)
		{
			engine.x_inc--;
			engine.y_inc++;
			engine.z_inc++;
			// screen.camera_x-=Math.sin(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_z+=Math.cos(screen.yrot*Math.PI/180)*Math.cos((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_y+=Math.sin((screen.xrot-180)*Math.PI/180)/10;
			// screen.camera_x+=Math.sin((screen.yrot-360)*Math.PI/360)/10;
			// screen.camera_y+=Math.sin((screen.xrot-360)*Math.PI/360)/10;
			// screen.camera_z-=Math.sin((screen.yrot-360)*Math.PI/360+Math.PI/2)/10;
			// System.out.println(screen.camera_z);
		}
		if(key.getKeyCode()==KeyEvent.VK_A)
		{
			engine.side_inc--;
		}
		if(key.getKeyCode()==KeyEvent.VK_D)
		{
			engine.side_inc++;
		}
		if(key.getKeyCode()==KeyEvent.VK_ESCAPE&&!pause_hold)
		{
			engine.mouse_trap=!engine.mouse_trap;
			pause_hold=true;
		}
		if(key.getKeyCode()==KeyEvent.VK_0)
		{
			engine.yrot_inc++;
		}
		if(key.getKeyCode()==KeyEvent.VK_C)
		{
			limit+=100;
		}
		if(key.getKeyCode()==KeyEvent.VK_V)
		{
			limit-=100;
		}
	}
	public void keyReleased(KeyEvent key) {
		if(key.getKeyCode()==KeyEvent.VK_ESCAPE)
			pause_hold=false;
		if(key.getKeyCode()==KeyEvent.VK_SPACE)
			change=false;
		if(key.getKeyCode()==KeyEvent.VK_B)
		{
			change1=false;
		}
	}
	public void mouseMoved(MouseEvent e)
	{
		if(!engine.mouse_trap) return;
		engine.xrot_inc+=(e.getY()-320);
		engine.yrot_inc+=(e.getX()-320);
//		screen.xrot+=(e.getY()-320);
//		screen.yrot+=(e.getX()-320);
//		if(screen.yrot>720)
//			screen.yrot-=720;
//		if(screen.yrot<0)
//			screen.yrot+=720;
//		if(screen.xrot>540)
//			screen.xrot=540;
//		if(screen.xrot<180)
//			screen.xrot=180;
	}
	public void mouseDragged(MouseEvent e)
	{

	}
	public void update() {
		// System.out.println(xvel);
//		System.out.println(jump_move);
	}
	public void keyTyped(KeyEvent arg0) {
		
	}
}