import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
public class object implements Comparable<object>
{
	double x1,y1,z1;
	double x2,y2,z2;
	double x3,y3,z3;
	int[][] pixels;
	int width,height;
	int pic_x1,pic_y1;
	int pic_x2,pic_y2;
	int pic_x3,pic_y3;
	surfaceNormal surfaceNormalOne;
	surfaceNormal surfaceNormalTwo;
	surfaceNormal surfaceNormalThree;
	double centerX,centerY,centerZ;
	double rotatedX,rotatedY,rotatedZ;
	double distance;
	int ID;
	// UVCoordinates uvCoordOne;
	// UVCoordinates uvCoordTwo;
	// UVCoordinates uvCoordThree;
	double uCoordOne,uCoordTwo,uCoordThree;
	double vCoordOne,vCoordTwo,vCoordThree;
	double diffusionOne,diffusionTwo,diffusionThree;
	double reflectVectorOneX,reflectVectorOneY,reflectVectorOneZ;
	double reflectVectorTwoX,reflectVectorTwoY,reflectVectorTwoZ;
	double reflectVectorThreeX,reflectVectorThreeY,reflectVectorThreeZ;
	shading shade;
	String name;
	double lightIntensityOneRed,lightIntensityOneGreen,lightIntensityOneBlue;
	double lightIntensityTwoRed,lightIntensityTwoGreen,lightIntensityTwoBlue;
	double lightIntensityThreeRed,lightIntensityThreeGreen,lightIntensityThreeBlue;
	public object(Vec3d VecOne,Vec3d VecTwo,Vec3d VecThree,surfaceNormal one,surfaceNormal two,surfaceNormal three,UVCoordinates a,UVCoordinates b,UVCoordinates c,shading d,String e)
	{
		name=e;
		x1=VecOne.one;
		y1=VecOne.two;
		z1=VecOne.three;
		x2=VecTwo.one;
		y2=VecTwo.two;
		z2=VecTwo.three;
		x3=VecThree.one;
		y3=VecThree.two;
		z3=VecThree.three;
		surfaceNormalOne=one;
		surfaceNormalTwo=two;
		surfaceNormalThree=three;
		centerX=(x1+x2+x3)/3;
		centerY=(y1+y2+y3)/3;
		centerZ=(z1+z2+z3)/3;
		// uvCoordOne=a;
		// uvCoordTwo=b;
		// uvCoordThree=c;
		uCoordOne=a.xVal;
		uCoordTwo=b.xVal;
		uCoordThree=c.xVal;
		vCoordOne=a.yVal;
		vCoordTwo=b.yVal;
		vCoordThree=c.yVal;
		double lightVectorOneX=screen.light.x1-x1;
		double lightVectorOneY=screen.light.y1-y1;
		double lightVectorOneZ=screen.light.z1-z1;
		double lightVectorOneDis=lightVectorOneX*lightVectorOneX+lightVectorOneY*lightVectorOneY+lightVectorOneZ*lightVectorOneZ;
		double approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorOneDis)/2);
		approx=approx*(3-approx*approx*lightVectorOneDis)/2;
		lightVectorOneX*=approx;
		lightVectorOneY*=approx;
		lightVectorOneZ*=approx;
		diffusionOne=lightVectorOneX*surfaceNormalOne.avgNormX+lightVectorOneY*surfaceNormalOne.avgNormY+lightVectorOneZ*surfaceNormalOne.avgNormZ;
		reflectVectorOneX=2*(lightVectorOneX*surfaceNormalOne.avgNormX+lightVectorOneY*surfaceNormalOne.avgNormY+lightVectorOneZ*surfaceNormalOne.avgNormZ)*surfaceNormalOne.avgNormX-lightVectorOneX;
		reflectVectorOneY=2*(lightVectorOneX*surfaceNormalOne.avgNormX+lightVectorOneY*surfaceNormalOne.avgNormY+lightVectorOneZ*surfaceNormalOne.avgNormZ)*surfaceNormalOne.avgNormY-lightVectorOneY;
		reflectVectorOneZ=2*(lightVectorOneX*surfaceNormalOne.avgNormX+lightVectorOneY*surfaceNormalOne.avgNormY+lightVectorOneZ*surfaceNormalOne.avgNormZ)*surfaceNormalOne.avgNormZ-lightVectorOneZ;

		double lightVectorTwoX=screen.light.x1-x2;
		double lightVectorTwoY=screen.light.y1-y2;
		double lightVectorTwoZ=screen.light.z1-z2;
		double lightVectorTwoDis=lightVectorTwoX*lightVectorTwoX+lightVectorTwoY*lightVectorTwoY+lightVectorTwoZ*lightVectorTwoZ;
		approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorTwoDis)/2);
		approx=approx*(3-approx*approx*lightVectorTwoDis)/2;
		lightVectorTwoX*=approx;
		lightVectorTwoY*=approx;
		lightVectorTwoZ*=approx;
		diffusionTwo=lightVectorTwoX*surfaceNormalTwo.avgNormX+lightVectorTwoY*surfaceNormalTwo.avgNormY+lightVectorTwoZ*surfaceNormalTwo.avgNormZ;
		reflectVectorTwoX=2*(lightVectorTwoX*surfaceNormalTwo.avgNormX+lightVectorTwoY*surfaceNormalTwo.avgNormY+lightVectorTwoZ*surfaceNormalTwo.avgNormZ)*surfaceNormalTwo.avgNormX-lightVectorTwoX;
		reflectVectorTwoY=2*(lightVectorTwoX*surfaceNormalTwo.avgNormX+lightVectorTwoY*surfaceNormalTwo.avgNormY+lightVectorTwoZ*surfaceNormalTwo.avgNormZ)*surfaceNormalTwo.avgNormY-lightVectorTwoY;
		reflectVectorTwoZ=2*(lightVectorTwoX*surfaceNormalTwo.avgNormX+lightVectorTwoY*surfaceNormalTwo.avgNormY+lightVectorTwoZ*surfaceNormalTwo.avgNormZ)*surfaceNormalTwo.avgNormZ-lightVectorTwoZ;

		double lightVectorThreeX=screen.light.x1-x3;
		double lightVectorThreeY=screen.light.y1-y3;
		double lightVectorThreeZ=screen.light.z1-z3;
		double lightVectorThreeDis=lightVectorThreeX*lightVectorThreeX+lightVectorThreeY*lightVectorThreeY+lightVectorThreeZ*lightVectorThreeZ;
		approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorThreeDis)/2);
		approx=approx*(3-approx*approx*lightVectorThreeDis)/2;
		lightVectorThreeX*=approx;
		lightVectorThreeY*=approx;
		lightVectorThreeZ*=approx;
		diffusionThree=lightVectorThreeX*surfaceNormalThree.avgNormX+lightVectorThreeY*surfaceNormalThree.avgNormY+lightVectorThreeZ*surfaceNormalThree.avgNormZ;
		reflectVectorThreeX=2*(lightVectorThreeX*surfaceNormalThree.avgNormX+lightVectorThreeY*surfaceNormalThree.avgNormY+lightVectorThreeZ*surfaceNormalThree.avgNormZ)*surfaceNormalThree.avgNormX-lightVectorThreeX;
		reflectVectorThreeY=2*(lightVectorThreeX*surfaceNormalThree.avgNormX+lightVectorThreeY*surfaceNormalThree.avgNormY+lightVectorThreeZ*surfaceNormalThree.avgNormZ)*surfaceNormalThree.avgNormY-lightVectorThreeY;
		reflectVectorThreeZ=2*(lightVectorThreeX*surfaceNormalThree.avgNormX+lightVectorThreeY*surfaceNormalThree.avgNormY+lightVectorThreeZ*surfaceNormalThree.avgNormZ)*surfaceNormalThree.avgNormZ-lightVectorThreeZ;

		shade=d;
		if(shade!=null)
		{
			// System.out.println("one x: "+uvCoordOne.xVal+" one y: "+uvCoordOne.yVal+" width: "+shade.width+" height: "+shade.height);
			width=shade.width;
			height=shade.height;
			pixels=shade.pixels;
			uCoordOne*=width;
			uCoordTwo*=width;
			uCoordThree*=width;
			vCoordOne*=height;
			vCoordTwo*=height;
			vCoordThree*=height;
			// System.out.println("new one x: "+uvCoordOne.xVal+" one y: "+uvCoordOne.yVal+" width: "+shade.width+" height: "+shade.height);
		}
		else
		{
			shade=new shading(null);
		}
	}
	public object(Vec3d VecOne,Vec3d VecTwo,Vec3d VecThree,surfaceNormal one,surfaceNormal two,surfaceNormal three,shading d,String e)
	{
		name=e;
		x1=VecOne.one;
		y1=VecOne.two;
		z1=VecOne.three;
		x2=VecTwo.one;
		y2=VecTwo.two;
		z2=VecTwo.three;
		x3=VecThree.one;
		y3=VecThree.two;
		z3=VecThree.three;
		surfaceNormalOne=one;
		surfaceNormalTwo=two;
		surfaceNormalThree=three;
		centerX=(x1+x2+x3)/3;
		centerY=(y1+y2+y3)/3;
		centerZ=(z1+z2+z3)/3;
		// uvCoordOne=a;
		// uvCoordTwo=b;
		// uvCoordThree=c;
		double lightVectorOneX=screen.light.x1-x1;
		double lightVectorOneY=screen.light.y1-y1;
		double lightVectorOneZ=screen.light.z1-z1;
		double lightVectorOneDis=lightVectorOneX*lightVectorOneX+lightVectorOneY*lightVectorOneY+lightVectorOneZ*lightVectorOneZ;
		double approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorOneDis)/2);
		approx=approx*(3-approx*approx*lightVectorOneDis)/2;
		lightVectorOneX*=approx;
		lightVectorOneY*=approx;
		lightVectorOneZ*=approx;
		diffusionOne=lightVectorOneX*surfaceNormalOne.avgNormX+lightVectorOneY*surfaceNormalOne.avgNormY+lightVectorOneZ*surfaceNormalOne.avgNormZ;

		double lightVectorTwoX=screen.light.x1-x2;
		double lightVectorTwoY=screen.light.y1-y2;
		double lightVectorTwoZ=screen.light.z1-z2;
		double lightVectorTwoDis=lightVectorTwoX*lightVectorTwoX+lightVectorTwoY*lightVectorTwoY+lightVectorTwoZ*lightVectorTwoZ;
		approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorTwoDis)/2);
		approx=approx*(3-approx*approx*lightVectorTwoDis)/2;
		lightVectorTwoX*=approx;
		lightVectorTwoY*=approx;
		lightVectorTwoZ*=approx;
		diffusionTwo=lightVectorTwoX*surfaceNormalTwo.avgNormX+lightVectorTwoY*surfaceNormalTwo.avgNormY+lightVectorTwoZ*surfaceNormalTwo.avgNormZ;

		double lightVectorThreeX=screen.light.x1-x3;
		double lightVectorThreeY=screen.light.y1-y3;
		double lightVectorThreeZ=screen.light.z1-z3;
		double lightVectorThreeDis=lightVectorThreeX*lightVectorThreeX+lightVectorThreeY*lightVectorThreeY+lightVectorThreeZ*lightVectorThreeZ;
		approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorThreeDis)/2);
		approx=approx*(3-approx*approx*lightVectorThreeDis)/2;
		lightVectorThreeX*=approx;
		lightVectorThreeY*=approx;
		lightVectorThreeZ*=approx;
		diffusionThree=lightVectorThreeX*surfaceNormalThree.avgNormX+lightVectorThreeY*surfaceNormalThree.avgNormY+lightVectorThreeZ*surfaceNormalThree.avgNormZ;
		// shade=new shading(null);
		shade=d;
		if(shade==null)
			shade=new shading(null);
	}
	public object(double x,double y,double z)
	{
		x1=x;
		y1=y;
		z1=z;
	}
	// public void add_picture(String loc,int w,int h)
	// {
	// 	pixels=new int[h*w];
	// 	BufferedImage image=null;
	// 	try
	// 	{
	// 		image=ImageIO.read(new File(loc));
	// 	}
	// 	catch(Exception e)
	// 	{
	// 		System.out.println("Error");
	// 	}
	// 	width=w;
	// 	height=h;
	// 	image.getRGB(0, 0, w, h, pixels, 0, w);
	// }
	public void setCorners(int a,int b,int c,int d,int e,int f)
	{
		pic_x1=a;
		pic_y1=b;
		pic_x2=c;
		pic_y2=d;
		pic_x3=e;
		pic_y3=f;
	}
	public String toString()
	{
		return " x1: "+x1+" y1: "+y1+" z1: "+z1+" x2: "+x2+" y2: "+y2+" z2: "+z2+" x3: "+x3+" y3: "+y3+" z3: "+z3;
	}
	public int compareTo(object a)
	{
		return -Double.compare(shade.transparent,a.shade.transparent);
	}
}