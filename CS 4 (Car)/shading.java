import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
class shading
{
	double ambientRed,ambientGreen,ambientBlue;
	double diffusionRed,diffusionGreen,diffusionBlue;
	double specularRed,specularGreen,specularBlue;
	double reflective;
	double transparent;
	String source;
	int height,width;
	int[][] pixels;
	String name;
	public shading(String one)
	{
		name=one;
		diffusionRed=diffusionBlue=diffusionGreen=1;
		transparent=1;
	}
	public void addAmbient(double a,double b,double c)
	{
		ambientRed=a;
		ambientGreen=b;
		ambientBlue=c;
	}
	public void addDiffusion(double a,double b,double c)
	{
		diffusionRed=a;
		diffusionGreen=b;
		diffusionBlue=c;
	}
	public void addSpecular(double a,double b,double c)
	{
		specularRed=a;
		specularGreen=b;
		specularBlue=c;
	}
	public void addTransparency(double a)
	{
		transparent=a;
	}
	public void addReflectiveness(double a)
	{
		reflective=a;
	}
	public void addSource(String a)
	{
		source=a;
		if(source.contains("tga"))
		{
			Path path = Paths.get(source);
			try
			{
				int[] tempPixels=TGAReader.read(Files.readAllBytes(path),TGAReader.ARGB);
				height=TGAReader.getHeight(Files.readAllBytes(path));
				width=TGAReader.getWidth(Files.readAllBytes(path));
				pixels=new int[height][width];
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						pixels[i][j]=tempPixels[i*width+j];
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			BufferedImage image=null;
			try
			{

				image=ImageIO.read(new File(a));
			}
			catch(Exception e)
			{
				System.out.println("Error");
			}
			height=image.getHeight();
			width=image.getWidth();
			int[] tempPixels=new int[height*width];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), tempPixels, 0, image.getWidth());
			pixels=new int[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					pixels[i][j]=tempPixels[i*width+j];
				}
			}
		}
	}
}