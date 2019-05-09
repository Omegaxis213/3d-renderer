import java.util.*;
import java.io.*;

public class test {
	public static void main(String[] args) throws Exception {
		int x=5;
		int y=7;
		x+=y+=x+=y++;
		System.out.println(x+" "+y);
		int a=5;
		int b=7;
		a=a+(b=b+(a=a+(b++)));
		System.out.println(a+" "+b);
		int c=4;
		c+=c++;
		System.out.println(c);
		c=4;
		c=c++ +c++;
		System.out.println(c);
		c=3;
		c=c+++c*c++;
		System.out.println(c);
		c=3;
		c=c + c++ + c++ + c++;
		System.out.println(c);
		c=1;
		c=c++*c+c++*c;
		System.out.println(c);
		c=3;
		c=c++*(c+c++);
		System.out.println(c);
		int d=5;
		int e=7;
		d+=e+=e+++(d+=e++);
		System.out.println(d+" "+e);
		int f=10;
		f+=f+=f+=f+=f;
		System.out.println(f);
		int g=10;
		g=g+++g;
		System.out.println(g);
		g=1;
		g=g+++ ++g*g+++ ++g;
		System.out.println(g);
	}

	static BufferedReader in;
	static PrintWriter out;
	static {
		try {
			in = new BufferedReader(new FileReader("test.in"));
		} catch (Exception e) {}
	}
	static StringTokenizer st;
	static int i() {return Integer.parseInt(st.nextToken());}
	static String s() {return st.nextToken();}
	static double d() {return Double.parseDouble(st.nextToken());}
	static long l() {return Long.parseLong(st.nextToken());}
}
abstract class A
{
	private int x;
	protected int y;
	public int z;
	private final int x1=1;
	private static int x2;
}