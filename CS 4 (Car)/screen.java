import java.util.*;
import java.io.*;
import java.awt.Color;
import java.nio.file.*;
public class screen {
	final int INFRONT=1;
	final int BEHIND=-1;
	final int SPANNING=2;
	final int COINCIDING=0;
	static ArrayList<object> objects=new ArrayList<object>();
	static object light;
	static int yrot=270;
	static int xrot=174,zrot=0;
	static boolean y_axis_rot;
	static boolean x_axis_rot;
	static boolean z_axis_rot;
	static double camera_x=0;
	static double camera_y=1,camera_z=0;
	static long temptime;
	static boolean destroy_area;
	static double[] sin_look_up=new double[361];
	static double[] cos_look_up=new double[361];
	static double cosYrot;
	static double sinYrot;
	static double cosXrot;
	static double sinXrot;
	static double sinXrotCosYrot;
	static double sinXrotSinYrot;
	static double cosXrotCosYrot;
	static double cosXrotSinYrot;

	double fps;
	ArrayList<Vec3d> points;
	ArrayList<surfaceNormal> surfaceNormals;
	ArrayList<UVCoordinates> uvCoords;
	HashMap<String,shading> shade;
	static int processed;
	int maximum;
	double[] zbuffer;
	boolean[] zbufferUsed;
	static binarySpacePartitionTree BSPTree;
	int totOccluded;
	int totTriangles;
	double rotLightX,rotLightY,rotLightZ;
	static int newPoints;
	segmentTree pixelsDrawn;
	int totNoDraw;
	int numOfTriangles;
	int maxNum;
	int maxNumOfVert;
	int numOfOccluders=1000;
	int prevCull;
	double[][][] HOMMaps;
	static HashSet<Integer> occluders;
	HashSet<Integer> newOccluders;
	ArrayList<object> pendingOccluders;
	Set<Integer> alwaysDraw;
	int numCulled;
	public screen() {
		objects=new ArrayList<object>();
		occluders=new HashSet<Integer>();
		newOccluders=new HashSet<Integer>();
		pendingOccluders=new ArrayList<object>();
		// STLParser tempo=new STLParser();
		int lineNum=0;
		try
		{
			// List<Triangle1> list1=tempo.parseSTLFile(FileSystems.getDefault().getPath("Ancient_Brass_Dragon.stl"));
			// for(int x=0;x<list1.size();x++)
			// {
			// 	object thing=new object(list1.get(x).one.one,list1.get(x).one.three,list1.get(x).one.two,list1.get(x).two.one,list1.get(x).two.three,list1.get(x).two.two,list1.get(x).three.one,list1.get(x).three.three,list1.get(x).three.two);
			// 	// System.out.print("num: "+x);
			// 	// System.out.println(thing);
			// 	objects.add(thing);
			// }
			light=new object(-50000000000L,50000000000L,0);
			points=new ArrayList<Vec3d>();
			surfaceNormals=new ArrayList<surfaceNormal>();
			uvCoords=new ArrayList<UVCoordinates>();
			// Path read=FileSystems.getDefault().getPath();
			String curGroup=null;
			shade=new HashMap<String,shading>();
			BufferedReader in=new BufferedReader(new FileReader("Avent.mtl"));
			shading cur=null;
			String curString=null;
			while(in.ready())
			{
				lineNum++;
				StringTokenizer st=new StringTokenizer(in.readLine());
				if(st.countTokens()==0) continue;
				String type=st.nextToken();
				if(type.equals("newmtl"))
				{
					if(curString!=null)
						shade.put(curString,cur);
					curString=st.nextToken();
					cur=new shading(curString);
				}
				else if(type.equals("Ka"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					double three=Double.parseDouble(st.nextToken());
					cur.addAmbient(one,two,three);
				}
				else if(type.equals("Kd"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					double three=Double.parseDouble(st.nextToken());
					cur.addDiffusion(one,two,three);
				}
				else if(type.equals("Ks"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					double three=Double.parseDouble(st.nextToken());
					cur.addSpecular(one,two,three);
				}
				else if(type.equals("map_Kd"))
				{
					String source="";
					while(st.hasMoreTokens())
						source+=st.nextToken()+" ";
					source=source.trim();
					cur.addSource(source);
				}
				else if(type.equals("d"))
				{
					cur.addTransparency(Double.parseDouble(st.nextToken()));
				}
				else if(type.equals("Ns"))
				{
					cur.addReflectiveness(Double.parseDouble(st.nextToken()));
				}
				else
				{
					System.out.println("not assigned: "+type);
				}
			}
			if(curString!=null)
				shade.put(curString,cur);
			String curObject=null;
			in=new BufferedReader(new FileReader("Avent.obj"));
			while(in.ready())
			{
				String temp=in.readLine();
				// System.out.println(temp);
				if(temp.isEmpty()) continue;
				StringTokenizer st=new StringTokenizer(temp);
				String type=st.nextToken();
				if(type.equals("v"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					double three=Double.parseDouble(st.nextToken());
					points.add(new Vec3d(one,two,three));
				}
				else if(type.equals("vn"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					double three=Double.parseDouble(st.nextToken());
					double dis=Math.sqrt(one*one+two*two+three*three);
					one/=dis;
					two/=dis;
					three/=dis;
					surfaceNormals.add(new surfaceNormal(one,two,three));
				}
				else if(type.equals("vt"))
				{
					double one=Double.parseDouble(st.nextToken());
					double two=Double.parseDouble(st.nextToken());
					uvCoords.add(new UVCoordinates(one,1-two));
				}
				else if(type.equals("f"))
				{
					int number=st.countTokens();
					String[][] arr=new String[number][];
					// String[] one=st.nextToken().split("[/]+");
					// String[] two=st.nextToken().split("[/]+");
					// String[] three=st.nextToken().split("[/]+");
					// System.out.println(Arrays.toString(one));
					for(int i=0;i<number;i++)
						arr[i]=st.nextToken().split("[/]+");
					if(arr[0].length==2)
					{
						for(int i=1;i<number-1;i++)
						{
							int pointPosOne=Integer.parseInt(arr[0][0])-1;
							int pointPosTwo=Integer.parseInt(arr[i][0])-1;
							int pointPosThree=Integer.parseInt(arr[i+1][0])-1;
							// int uvCoordPosOne=Integer.parseInt(one[1])-1;
							// int uvCoordPosTwo=Integer.parseInt(two[1])-1;
							// int uvCoordPosThree=Integer.parseInt(three[1])-1;
							int normalPosOne=Integer.parseInt(arr[0][1])-1;
							int normalPosTwo=Integer.parseInt(arr[i][1])-1;
							int normalPosThree=Integer.parseInt(arr[i+1][1])-1;
							objects.add(new object(points.get(pointPosOne),points.get(pointPosTwo),points.get(pointPosThree),surfaceNormals.get(normalPosOne),surfaceNormals.get(normalPosTwo),surfaceNormals.get(normalPosThree),shade.get(curGroup),curObject));
						}
					}
					else
					{
						for(int i=1;i<number-1;i++)
						{
							int pointPosOne=Integer.parseInt(arr[0][0])-1;
							int pointPosTwo=Integer.parseInt(arr[i][0])-1;
							int pointPosThree=Integer.parseInt(arr[i+1][0])-1;
							int uvCoordPosOne=Integer.parseInt(arr[0][1])-1;
							int uvCoordPosTwo=Integer.parseInt(arr[i][1])-1;
							int uvCoordPosThree=Integer.parseInt(arr[i+1][1])-1;
							int normalPosOne=Integer.parseInt(arr[0][2])-1;
							int normalPosTwo=Integer.parseInt(arr[i][2])-1;
							int normalPosThree=Integer.parseInt(arr[i+1][2])-1;
							objects.add(new object(points.get(pointPosOne),points.get(pointPosTwo),points.get(pointPosThree),surfaceNormals.get(normalPosOne),surfaceNormals.get(normalPosTwo),surfaceNormals.get(normalPosThree),uvCoords.get(uvCoordPosOne),uvCoords.get(uvCoordPosTwo),uvCoords.get(uvCoordPosThree),shade.get(curGroup),curObject));
						}
					}
				}
				else if(type.equals("usemtl"))
				{
					curGroup=st.nextToken();
				}
				else if(type.equals("o"))
				{
					curObject=st.nextToken();
				}
			}
			Collections.sort(objects);
			System.out.println(objects.size());
			// System.out.println(list1.size());
			// for(int x=0;x<list1.size();x++)
			// {
				// System.out.println("vertex1: "+list1.get(x).one);
				// System.out.println("vertex2: "+list1.get(x).two);
				// System.out.println("vertex3: "+list1.get(x).three);
				// System.out.println();
			// }
		}
		catch(Exception e)
		{
			// System.out.println(e.getCause());
			e.printStackTrace();
			System.out.println(lineNum);
			System.out.println("ERROR IN READING FILE");
		}
		polygon[] tempArr=new polygon[objects.size()];
		double maxTotError=0;
		for (int i = 0; i < objects.size(); i++) {
			Vec3d[] tempPoints=new Vec3d[3];
			Vec3d one=new Vec3d(objects.get(i).x1,objects.get(i).y1,objects.get(i).z1);
			Vec3d two=new Vec3d(objects.get(i).x2,objects.get(i).y2,objects.get(i).z2);
			Vec3d three=new Vec3d(objects.get(i).x3,objects.get(i).y3,objects.get(i).z3);
			tempPoints[0]=one;
			tempPoints[1]=two;
			tempPoints[2]=three;
			double Vx=objects.get(i).x2-objects.get(i).x1;
			double Vy=objects.get(i).y2-objects.get(i).y1;
			double Vz=objects.get(i).z2-objects.get(i).z1;

			double Wx=objects.get(i).x3-objects.get(i).x1;
			double Wy=objects.get(i).y3-objects.get(i).y1;
			double Wz=objects.get(i).z3-objects.get(i).z1;

			double normalX=(Vy*Wz)-(Vz*Wy);
			double normalY=(Vz*Wx)-(Vx*Wz);
			double normalZ=(Vx*Wy)-(Vy*Wx);
			// System.out.println(normalX+" "+normalY+" "+normalZ);
			double dis=normalX*one.one+normalY*one.two+normalZ*one.three;
			double errorOne=Math.abs(dis-normalX*one.one-normalY*one.two-normalZ*one.three);
			double errorTwo=Math.abs(dis-normalX*two.one-normalY*two.two-normalZ*two.three);
			double errorThree=Math.abs(dis-normalX*three.one-normalY*three.two-normalZ*three.three);
			double maxError=Math.max(errorOne,Math.max(errorTwo,errorThree));
			maxTotError=Math.max(maxTotError,maxError);
			// System.out.println(maxError);
			// System.out.println(errorOne+" "+errorTwo+" "+errorThree);
			tempArr[i]=new polygon(tempPoints,one,two,three,objects.get(i).surfaceNormalOne,objects.get(i).surfaceNormalTwo,objects.get(i).surfaceNormalThree,new surfaceNormal(normalX,normalY,normalZ),dis);
		}
		// System.out.println(maxTotError);
		long tempTime=System.nanoTime();
		// BSPTree=new binarySpacePartitionTree(tempArr,tempArr.length);
		// visit(BSPTree);
		// System.out.println(newPoints);
		// System.out.println("pre init time: "+(System.nanoTime()-tempTime)/1E9+" seconds");
		// System.out.println(maximum);
		// light=new object(1.25,70,-3.25);
		// light=new object(0.25,-3,0.25);
		for(int x=0;x<=360;x++)
		{
			sin_look_up[x%360]=Math.sin(-x*Math.PI/180);
			cos_look_up[x%360]=Math.cos(-x*Math.PI/180);
		}
	}
	public void visit(binarySpacePartitionTree cur)
	{
		if(cur==null||cur.current==null) return;
		// System.out.println(cur+" "+cur.behind+" "+cur.inFront+" "+Arrays.toString(cur.current));
		visit(cur.behind);
		for (int i = 0; i < cur.current.length; i++) {
			if(cur.current[i]==null) break;
			ArrayList<triangle> tempList=new ArrayList<triangle>();
			Vec3d one=cur.current[i].points[0];
			Vec3d two=cur.current[i].points[1];
			for (int j = 2; j < cur.current[i].points.length; j++) {
				Vec3d three=cur.current[i].points[j];
				double origOneX=cur.current[i].origPointOne.one/cur.current[i].origPointOne.three*640+320;
				double origOneY=cur.current[i].origPointOne.two/cur.current[i].origPointOne.three*640+320;
				double origTwoX=cur.current[i].origPointTwo.one/cur.current[i].origPointTwo.three*640+320;
				double origTwoY=cur.current[i].origPointTwo.two/cur.current[i].origPointTwo.three*640+320;
				double origThreeX=cur.current[i].origPointThree.one/cur.current[i].origPointThree.three*640+320;
				double origThreeY=cur.current[i].origPointThree.two/cur.current[i].origPointThree.three*640+320;

				double oneNewX=one.one/one.three*640+320;
				double oneNewY=one.two/one.three*640+320;
				double twoNewX=two.one/two.three*640+320;
				double twoNewY=two.two/two.three*640+320;
				double threeNewX=three.one/three.three*640+320;
				double threeNewY=three.two/three.three*640+320;
				double denom=(origTwoY-origThreeY)*(origOneX-origThreeX)+(origThreeX-origTwoX)*(origOneY-origThreeY);
				double weight_one=((origTwoY-origThreeY)*(oneNewX-origThreeX)+(origThreeX-origTwoX)*(oneNewY-origThreeY))/denom;
				double weight_two=((origThreeY-origOneY)*(oneNewX-origThreeX)+(origOneX-origThreeX)*(oneNewY-origThreeY))/denom;
				double weight_three=1-weight_one-weight_two;
				double normX=cur.current[i].surfaceNormalOne.avgNormX*weight_one+cur.current[i].surfaceNormalTwo.avgNormX*weight_two+cur.current[i].surfaceNormalThree.avgNormX*weight_three;
				double normY=cur.current[i].surfaceNormalOne.avgNormY*weight_one+cur.current[i].surfaceNormalTwo.avgNormY*weight_two+cur.current[i].surfaceNormalThree.avgNormY*weight_three;
				double normZ=cur.current[i].surfaceNormalOne.avgNormZ*weight_one+cur.current[i].surfaceNormalTwo.avgNormZ*weight_two+cur.current[i].surfaceNormalThree.avgNormZ*weight_three;
				surfaceNormal tempOne=new surfaceNormal(normX,normY,normZ);
				weight_one=((origTwoY-origThreeY)*(twoNewX-origThreeX)+(origThreeX-origTwoX)*(twoNewY-origThreeY))/denom;
				weight_two=((origThreeY-origOneY)*(twoNewX-origThreeX)+(origOneX-origThreeX)*(twoNewY-origThreeY))/denom;
				weight_three=1-weight_one-weight_two;
				normX=cur.current[i].surfaceNormalOne.avgNormX*weight_one+cur.current[i].surfaceNormalTwo.avgNormX*weight_two+cur.current[i].surfaceNormalThree.avgNormX*weight_three;
				normY=cur.current[i].surfaceNormalOne.avgNormY*weight_one+cur.current[i].surfaceNormalTwo.avgNormY*weight_two+cur.current[i].surfaceNormalThree.avgNormY*weight_three;
				normZ=cur.current[i].surfaceNormalOne.avgNormZ*weight_one+cur.current[i].surfaceNormalTwo.avgNormZ*weight_two+cur.current[i].surfaceNormalThree.avgNormZ*weight_three;
				surfaceNormal tempTwo=new surfaceNormal(normX,normY,normZ);
				weight_one=((origTwoY-origThreeY)*(threeNewX-origThreeX)+(origThreeX-origTwoX)*(threeNewY-origThreeY))/denom;
				weight_two=((origThreeY-origOneY)*(threeNewX-origThreeX)+(origOneX-origThreeX)*(threeNewY-origThreeY))/denom;
				weight_three=1-weight_one-weight_two;
				normX=cur.current[i].surfaceNormalOne.avgNormX*weight_one+cur.current[i].surfaceNormalTwo.avgNormX*weight_two+cur.current[i].surfaceNormalThree.avgNormX*weight_three;
				normY=cur.current[i].surfaceNormalOne.avgNormY*weight_one+cur.current[i].surfaceNormalTwo.avgNormY*weight_two+cur.current[i].surfaceNormalThree.avgNormY*weight_three;
				normZ=cur.current[i].surfaceNormalOne.avgNormZ*weight_one+cur.current[i].surfaceNormalTwo.avgNormZ*weight_two+cur.current[i].surfaceNormalThree.avgNormZ*weight_three;
				surfaceNormal tempThree=new surfaceNormal(normX,normY,normZ);

				triangle tempTriangle=new triangle(cur.current[i].origPointOne,cur.current[i].origPointTwo,cur.current[i].origPointThree,one,two,three,tempOne,tempTwo,tempThree);
				tempList.add(tempTriangle);
				two=three;
			}
			cur.drawTriangle.add(tempList);
			// cur.current[i].createBoundingBox();
		}
		// for (int i = 0; i < cur.current.length; i++) {
		// 	if(cur.current[i]==null) break;
		// 	maximum=Math.max(maximum,cur.current[i].points.length);
		// }
		// if(cur!=null)
		// 	System.out.println(cur.current[0].points.length);
		visit(cur.inFront);
	}
	public int[] update(int[] pixels) {
		alwaysDraw=new HashSet<Integer>();
		Arrays.fill(pixels,-1);
		zbuffer=new double[pixels.length];
		zbufferUsed=new boolean[pixels.length];
		// Arrays.fill(zbuffer,1<<20);
		int counter=0;
		int total=0;
		// pixelsDrawn=new segmentTree();
		// ArrayList<surfaceNormal> normals=new ArrayList<surfaceNormal>();
		HOMMaps=new double[8][640][640];
		occluders=newOccluders;
		// Queue<object> curOccluders=new LinkedList<object>();
		// for (int i = 0; i < objects.size(); i++) {
		// 	double rotX=cosYrot*(objects.get(i).centerX-camera_x)-sinYrot*(objects.get(i).centerZ-camera_z);
		// 	double rotY=sinXrotCosYrot*(objects.get(i).centerZ-camera_z)+sinXrotSinYrot*(objects.get(i).centerX-camera_x)+cosXrot*(objects.get(i).centerY-camera_y);
		// 	double rotZ=cosXrotCosYrot*(objects.get(i).centerZ-camera_z)+cosXrotSinYrot*(objects.get(i).centerX-camera_x)-sinXrot*(objects.get(i).centerY-camera_y);
		// 	double dis=rotX*rotX+rotY*rotY+rotZ*rotZ;
		// 	objects.get(i).distance=dis;
		// 	objects.get(i).rotatedX=rotX;
		// 	objects.get(i).rotatedY=rotY;
		// 	objects.get(i).rotatedZ=rotZ;
		// }
		// Collections.sort(objects);
		// Queue<object> curOccluders=new LinkedList<object>();
		// int pollTimes=0;
		// boolean[] used=new boolean[objects.size()];
		// for(object a:occluders)
		// {
		// 	curOccluders.add(a);
		// 	alwaysDraw.add(a.ID);
		// 	used[a.ID]=true;
		// 	if(curOccluders.size()>=numOfOccluders)
		// 	{
		// 		alwaysDraw.remove(a.ID);
		// 		used[a.ID]=false;
		// 		curOccluders.poll();
		// 		pollTimes++;
		// 	}
		// }
		// for (int i = 0; i < pollTimes; i++) {
		// 	occluders.poll();
		// }
		// for(object a:pendingOccluders)
		// {
		// 	curOccluders.add(a);
		// 	alwaysDraw.add(a.ID);
		// 	used[a.ID]=true;
		// 	if(curOccluders.size()>=numOfOccluders)
		// 	{
		// 		alwaysDraw.remove(a.ID);
		// 		used[a.ID]=false;
		// 		curOccluders.poll();
		// 	}
		// }
		// for (int i = 0; i < objects.size(); i++) {
		// 	double centerNewX=objects.get(i).rotatedX/objects.get(i).rotatedZ;//in screen if within -.5 and .5
		// 	double centerNewY=objects.get(i).rotatedY/objects.get(i).rotatedZ;//in screen if within -.5 and .5
		// 	if(centerNewY+10<0||centerNewY-10>640||centerNewX+10<0||centerNewX-10>640) continue;
		// 	if(curOccluders.size()>=numOfOccluders) break;
		// 	alwaysDraw.add(objects.get(i).ID);
		// 	curOccluders.add(objects.get(i));
		// 	used[objects.get(i).ID]=true;
		// }
		// double[][] zbuffer=new double[640][640];
		// for (int i = 0; i < 640; i++) {
		// 	Arrays.fill(zbuffer[i],1<<20);
		// }
		long tempTime=System.nanoTime();
		for(int b:newOccluders)
		{
			object a=objects.get(b);
			double oneX=cosYrot*(a.x1-camera_x)-sinYrot*(a.z1-camera_z);
			double oneY=sinXrotCosYrot*(a.z1-camera_z)+sinXrotSinYrot*(a.x1-camera_x)+cosXrot*(a.y1-camera_y);
			double oneZ=cosXrotCosYrot*(a.z1-camera_z)+cosXrotSinYrot*(a.x1-camera_x)-sinXrot*(a.y1-camera_y);

			double twoX=cosYrot*(a.x2-camera_x)-sinYrot*(a.z2-camera_z);
			double twoY=sinXrotCosYrot*(a.z2-camera_z)+sinXrotSinYrot*(a.x2-camera_x)+cosXrot*(a.y2-camera_y);
			double twoZ=cosXrotCosYrot*(a.z2-camera_z)+cosXrotSinYrot*(a.x2-camera_x)-sinXrot*(a.y2-camera_y);

			double threeX=cosYrot*(a.x3-camera_x)-sinYrot*(a.z3-camera_z);
			double threeY=sinXrotCosYrot*(a.z3-camera_z)+sinXrotSinYrot*(a.x3-camera_x)+cosXrot*(a.y3-camera_y);
			double threeZ=cosXrotCosYrot*(a.z3-camera_z)+cosXrotSinYrot*(a.x3-camera_x)-sinXrot*(a.y3-camera_y);

			double newOneX=oneX/oneZ*640+320;
			double newOneY=oneY/oneZ*640+320;

			double newTwoX=twoX/twoZ*640+320;
			double newTwoY=twoY/twoZ*640+320;

			double newThreeX=threeX/threeZ*640+320;
			double newThreeY=threeY/threeZ*640+320;

			double minY=Math.min(newOneY,Math.min(newTwoY,newThreeY));
			double maxY=Math.max(newOneY,Math.max(newTwoY,newThreeY));
			minY=Math.max(0,minY);
			maxY=Math.min(640,maxY);
			double denom=(newTwoY-newThreeY)*(newOneX-newThreeX)+(newThreeX-newTwoX)*(newOneY-newThreeY);
			if(denom==0) continue;

			// double surfaceNormalOneX=cosYrot*(a.surfaceNormalOne.avgNormX)-sinYrot*(a.surfaceNormalOne.avgNormZ);
			// double surfaceNormalOneY=sinXrotCosYrot*(a.surfaceNormalOne.avgNormZ)+sinXrotSinYrot*(a.surfaceNormalOne.avgNormX)+cosXrot*(a.surfaceNormalOne.avgNormY);
			// double surfaceNormalOneZ=cosXrotCosYrot*(a.surfaceNormalOne.avgNormZ)+cosXrotSinYrot*(a.surfaceNormalOne.avgNormX)-sinXrot*(a.surfaceNormalOne.avgNormY);

			// double surfaceNormalTwoX=cosYrot*(a.surfaceNormalTwo.avgNormX)-sinYrot*(a.surfaceNormalTwo.avgNormZ);
			// double surfaceNormalTwoY=sinXrotCosYrot*(a.surfaceNormalTwo.avgNormZ)+sinXrotSinYrot*(a.surfaceNormalTwo.avgNormX)+cosXrot*(a.surfaceNormalTwo.avgNormY);
			// double surfaceNormalTwoZ=cosXrotCosYrot*(a.surfaceNormalTwo.avgNormZ)+cosXrotSinYrot*(a.surfaceNormalTwo.avgNormX)-sinXrot*(a.surfaceNormalTwo.avgNormY);

			// double surfaceNormalThreeX=cosYrot*(a.surfaceNormalThree.avgNormX)-sinYrot*(a.surfaceNormalThree.avgNormZ);
			// double surfaceNormalThreeY=sinXrotCosYrot*(a.surfaceNormalThree.avgNormZ)+sinXrotSinYrot*(a.surfaceNormalThree.avgNormX)+cosXrot*(a.surfaceNormalThree.avgNormY);
			// double surfaceNormalThreeZ=cosXrotCosYrot*(a.surfaceNormalThree.avgNormZ)+cosXrotSinYrot*(a.surfaceNormalThree.avgNormX)-sinXrot*(a.surfaceNormalThree.avgNormY);

			double weight_one_inc_1=(newTwoY-newThreeY)/denom;
			double weight_two_inc_1=(newThreeY-newOneY)/denom;
			double weight_three_inc_1=(newOneY-newTwoY)/denom;
			double weight_one_inc_2=(newThreeX-newTwoX)/denom;
			double weight_two_inc_2=(newOneX-newThreeX)/denom;
			double weight_three_inc_2=(newTwoX-newOneX)/denom;
			
			// double lightVectorOneX=rotLightX-oneX;
			// double lightVectorOneY=rotLightY-oneY;
			// double lightVectorOneZ=rotLightZ-oneZ;
			// double lightVectorOneDis=lightVectorOneX*lightVectorOneX+lightVectorOneY*lightVectorOneY+lightVectorOneZ*lightVectorOneZ;
			// double approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorOneDis)/2);
			// approx=approx*(3-approx*approx*lightVectorOneDis)/2;
			// lightVectorOneX*=approx;
			// lightVectorOneY*=approx;
			// lightVectorOneZ*=approx;
			// double diffusionOne=lightVectorOneX*surfaceNormalOneX+lightVectorOneY*surfaceNormalOneY+lightVectorOneZ*surfaceNormalOneZ;
			// double reflectOne=0;
			// double lightIntensityOne=diffusionOne*255+reflectOne*255;

			// double lightVectorTwoX=rotLightX-twoX;
			// double lightVectorTwoY=rotLightY-twoY;
			// double lightVectorTwoZ=rotLightZ-twoZ;
			// double lightVectorTwoDis=lightVectorTwoX*lightVectorTwoX+lightVectorTwoY*lightVectorTwoY+lightVectorTwoZ*lightVectorTwoZ;
			// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorTwoDis)/2);
			// approx=approx*(3-approx*approx*lightVectorTwoDis)/2;
			// lightVectorTwoX*=approx;
			// lightVectorTwoY*=approx;
			// lightVectorTwoZ*=approx;
			// double diffusionTwo=lightVectorTwoX*surfaceNormalTwoX+lightVectorTwoY*surfaceNormalTwoY+lightVectorTwoZ*surfaceNormalTwoZ;
			// double reflectTwo=0;
			// double lightIntensityTwo=diffusionTwo*255+reflectTwo*255;

			// double lightVectorThreeX=rotLightX-threeX;
			// double lightVectorThreeY=rotLightY-threeY;
			// double lightVectorThreeZ=rotLightZ-threeZ;
			// double lightVectorThreeDis=lightVectorThreeX*lightVectorThreeX+lightVectorThreeY*lightVectorThreeY+lightVectorThreeZ*lightVectorThreeZ;
			// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorThreeDis)/2);
			// approx=approx*(3-approx*approx*lightVectorThreeDis)/2;
			// lightVectorThreeX*=approx;
			// lightVectorThreeY*=approx;
			// lightVectorThreeZ*=approx;
			// double diffusionThree=lightVectorThreeX*surfaceNormalThreeX+lightVectorThreeY*surfaceNormalThreeY+lightVectorThreeZ*surfaceNormalThreeZ;
			// double reflectThree=0;
			// double lightIntensityThree=diffusionThree*255+reflectThree*255;

			double weight_one=((newTwoY-newThreeY)*(-newThreeX)+(newThreeX-newTwoX)*((int)minY-newThreeY))/denom;
			double weight_two=((newThreeY-newOneY)*(-newThreeX)+(newOneX-newThreeX)*((int)minY-newThreeY))/denom;
			double weight_three=1-weight_one-weight_two;
			// double lightIntensityInc1=weight_one_inc_1*lightIntensityOne+weight_two_inc_1*lightIntensityTwo+weight_three_inc_1*lightIntensityThree;
			// double lightIntensityInc2=weight_one_inc_2*lightIntensityOne+weight_two_inc_2*lightIntensityTwo+weight_three_inc_2*lightIntensityThree;
			// double lightIntensityCur=weight_one*lightIntensityOne+weight_two*lightIntensityTwo+weight_three*lightIntensityThree;
			double inverseWeightOneInc=1/weight_one_inc_1;
			double inverseWeightTwoInc=1/weight_two_inc_1;
			double inverseWeightThreeInc=1/weight_three_inc_1;
			double disToZeroInc1=-weight_one_inc_2*inverseWeightOneInc;
			double disToZeroInc2=-weight_two_inc_2*inverseWeightTwoInc;
			double disToZeroInc3=-weight_three_inc_2*inverseWeightThreeInc;
			double disToZero1=-weight_one*inverseWeightOneInc;
			double disToZero2=-weight_two*inverseWeightTwoInc;
			double disToZero3=-weight_three*inverseWeightThreeInc;
			// double interpolated_inc=weight_one_inc_1*oneZ+weight_two_inc_1*twoZ+weight_three_inc_1*threeZ;
			// double interpolated_inc_2=weight_one_inc_2*oneZ+weight_two_inc_2*twoZ+weight_three_inc_2*threeZ;
			// double interpolated_z_cur=oneZ*weight_one+twoZ*weight_two+threeZ*weight_three;
			for (int y = (int)minY; y < maxY; y++) {
				double inc_1=weight_one_inc_1;
				double inc_2=weight_two_inc_1;
				double inc_3=weight_three_inc_1;

				double one=Math.ceil(disToZero1);
				double two=Math.ceil(disToZero2);
				double three=Math.ceil(disToZero3);

				if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0))
				{
					// lightIntensityCur+=lightIntensityInc2;
					// interpolated_z_cur+=interpolated_inc_2;
					disToZero1+=disToZeroInc1;
					disToZero2+=disToZeroInc2;
					disToZero3+=disToZeroInc3;
					continue;
				}
				// double interpolated_z=interpolated_z_cur;
				// double lightIntensity=lightIntensityCur;

				int min=0;
				if(inc_1>0&&one>0)
					min=Math.max(min,(int)one);
				if(inc_2>0&&two>0)
					min=Math.max(min,(int)two);
				if(inc_3>0&&three>0)
					min=Math.max(min,(int)three);
				// lightIntensity+=lightIntensityInc1*min;
				// interpolated_z+=interpolated_inc*min;
				int max=640;
				// if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0)) max=0;
				if(inc_1<0)
					max=Math.min(max,(int)one);
				if(inc_2<0)
					max=Math.min(max,(int)two);
				if(inc_3<0)
					max=Math.min(max,(int)three);
				min=Math.max(0,min);
				max=Math.min(639,max);
				for (int x = min; x <= max; x++) {
					HOMMaps[7][y][x]=1;
					// if(zbuffer[y][x]>interpolated_z)
					// {
					// 	zbuffer[y][x]=interpolated_z;
					// 	// if(lightIntensity<0)
					// 	// 	pixels[y*640+x]=0;
					// 	// else if(lightIntensity>255)
					// 	// {
					// 	// 	pixels[y*640+x]=0b111111111111111111111111;
					// 	// 	// pixels[y*640+x]=0b111111110000000000000000;
					// 	// }
					// 	// else
					// 	// {
					// 	// 	int red=(int)lightIntensity<<16;
					// 	// 	int green=(int)lightIntensity<<8;
					// 	// 	int blue=(int)lightIntensity;
					// 	// 	pixels[y*640+x]=red+green+blue;
					// 	// 	// pixels[y*640+x]=red;
					// 	// }
					// }
					// lightIntensity+=lightIntensityInc1;
					// interpolated_z+=interpolated_inc;
				}
				// lightIntensityCur+=lightIntensityInc2;
				// interpolated_z_cur+=interpolated_inc_2;
				disToZero1+=disToZeroInc1;
				disToZero2+=disToZeroInc2;
				disToZero3+=disToZeroInc3;
			}
		}
		// int maxY=320;
		// int maxX=320;
		// for (int i = 6; i >= 0; i--) {
		// 	for (int y = 0; y < maxY; y++) {
		// 		for (int x = 0; x < maxX; x++) {
		// 			HOMMaps[i][y][x]=(HOMMaps[i+1][y*2][x*2]+HOMMaps[i+1][y*2][x*2+1]+HOMMaps[i+1][y*2+1][x*2]+HOMMaps[i+1][y*2+1][x*2+1])/4;
		// 		}
		// 	}
		// 	maxY/=2;
		// 	maxX/=2;
		// }
		// System.out.println("time initialized HOM: "+(System.nanoTime()-tempTime));

		//vector u is point 2 - point 1, vector v is point 3 - point 1
		double UOneX=1-0;
		double UOneY=1-0;
		double UOneZ=1-0;
		double VOneX=1-0;
		double VOneY=-1-0;
		double VOneZ=1-0;

		double normalOneX=UOneY*VOneZ-UOneZ*VOneY;
		double normalOneY=UOneZ*VOneX-UOneX*VOneZ;
		double normalOneZ=UOneX*VOneY-UOneY*VOneX;

		double disOne=Math.sqrt(normalOneX*normalOneX+normalOneY*normalOneY+normalOneZ*normalOneZ);
		normalOneX/=disOne;
		normalOneY/=disOne;
		normalOneZ/=disOne;

		//vector u is point 2 - point 1, vector v is point 3 - point 1
		double UTwoX=-1-0;
		double UTwoY=1-0;
		double UTwoZ=1-0;
		double VTwoX=1-0;
		double VTwoY=1-0;
		double VTwoZ=1-0;

		double normalTwoX=UTwoY*VTwoZ-UTwoZ*VTwoY;
		double normalTwoY=UTwoZ*VTwoX-UTwoX*VTwoZ;
		double normalTwoZ=UTwoX*VTwoY-UTwoY*VTwoX;

		double disTwo=Math.sqrt(normalTwoX*normalTwoX+normalTwoY*normalTwoY+normalTwoZ*normalTwoZ);
		normalTwoX/=disTwo;
		normalTwoY/=disTwo;
		normalTwoZ/=disTwo;

		//vector u is point 2 - point 1, vector v is point 3 - point 1
		double UThreeX=-1-0;
		double UThreeY=-1-0;
		double UThreeZ=1-0;
		double VThreeX=-1-0;
		double VThreeY=1-0;
		double VThreeZ=1-0;

		double normalThreeX=UThreeY*VThreeZ-UThreeZ*VThreeY;
		double normalThreeY=UThreeZ*VThreeX-UThreeX*VThreeZ;
		double normalThreeZ=UThreeX*VThreeY-UThreeY*VThreeX;

		double disThree=Math.sqrt(normalThreeX*normalThreeX+normalThreeY*normalThreeY+normalThreeZ*normalThreeZ);
		normalThreeX/=disThree;
		normalThreeY/=disThree;
		normalThreeZ/=disThree;

		//vector u is point 2 - point 1, vector v is point 3 - point 1
		double UFourX=1-0;
		double UFourY=-1-0;
		double UFourZ=1-0;
		double VFourX=-1-0;
		double VFourY=-1-0;
		double VFourZ=1-0;

		double normalFourX=UFourY*VFourZ-UFourZ*VFourY;
		double normalFourY=UFourZ*VFourX-UFourX*VFourZ;
		double normalFourZ=UFourX*VFourY-UFourY*VFourX;

		double disFour=Math.sqrt(normalFourX*normalFourX+normalFourY*normalFourY+normalFourZ*normalFourZ);
		normalFourX/=disFour;
		normalFourY/=disFour;
		normalFourZ/=disFour;
		// System.out.println("1: x: "+normalOneX+" y: "+normalOneY+" z: "+normalOneZ);
		// System.out.println("2: x: "+normalTwoX+" y: "+normalTwoY+" z: "+normalTwoZ);
		// System.out.println("3: x: "+normalThreeX+" y: "+normalThreeY+" z: "+normalThreeZ);
		// System.out.println("4: x: "+normalFourX+" y: "+normalFourY+" z: "+normalFourZ);

		long timeCount=System.nanoTime();
		totOccluded=0;
		totTriangles=0;
		totNoDraw=0;
		numOfTriangles=0;
		maxNumOfVert=0;
		numCulled=0;
		int totDrawn=0;
		newOccluders=new HashSet<Integer>();
		for(int z=0;z<objects.size();z++)
		{
			double oneX=cosYrot*(objects.get(z).x1-camera_x)-sinYrot*(objects.get(z).z1-camera_z);
			double oneY=sinXrotCosYrot*(objects.get(z).z1-camera_z)+sinXrotSinYrot*(objects.get(z).x1-camera_x)+cosXrot*(objects.get(z).y1-camera_y);
			double oneZ=cosXrotCosYrot*(objects.get(z).z1-camera_z)+cosXrotSinYrot*(objects.get(z).x1-camera_x)-sinXrot*(objects.get(z).y1-camera_y);

			double twoX=cosYrot*(objects.get(z).x2-camera_x)-sinYrot*(objects.get(z).z2-camera_z);
			double twoY=sinXrotCosYrot*(objects.get(z).z2-camera_z)+sinXrotSinYrot*(objects.get(z).x2-camera_x)+cosXrot*(objects.get(z).y2-camera_y);
			double twoZ=cosXrotCosYrot*(objects.get(z).z2-camera_z)+cosXrotSinYrot*(objects.get(z).x2-camera_x)-sinXrot*(objects.get(z).y2-camera_y);

			double threeX=cosYrot*(objects.get(z).x3-camera_x)-sinYrot*(objects.get(z).z3-camera_z);
			double threeY=sinXrotCosYrot*(objects.get(z).z3-camera_z)+sinXrotSinYrot*(objects.get(z).x3-camera_x)+cosXrot*(objects.get(z).y3-camera_y);
			double threeZ=cosXrotCosYrot*(objects.get(z).z3-camera_z)+cosXrotSinYrot*(objects.get(z).x3-camera_x)-sinXrot*(objects.get(z).y3-camera_y);


			//vector u is point 2 - point 1, vector v is point 3 - point 1
			double uX=twoX-oneX;
			double uY=twoY-oneY;
			double uZ=twoZ-oneZ;
			double vX=threeX-oneX;
			double vY=threeY-oneY;
			double vZ=threeZ-oneZ;

			double normalX=uY*vZ-uZ*vY;
			double normalY=uZ*vX-uX*vZ;
			double normalZ=uX*vY-uY*vX;

			if((objects.get(z).shade.name==null||!objects.get(z).shade.name.equals("Glass"))&&(normalX*oneX+normalY*oneY+normalZ*oneZ)>0) continue;

			double bbMinX=Math.min(oneX,Math.min(twoX,threeX));
			double bbMinY=Math.min(oneY,Math.min(twoY,threeY));
			double bbMinZ=Math.min(oneZ,Math.min(twoZ,threeZ));
			double bbMaxX=Math.max(oneX,Math.max(twoX,threeX));
			double bbMaxY=Math.max(oneY,Math.max(twoY,threeY));
			double bbMaxZ=Math.max(oneZ,Math.max(twoZ,threeZ));
			double centerBBX=(bbMinX+bbMaxX)/2;
			double centerBBY=(bbMinY+bbMaxY)/2;
			double centerBBZ=(bbMinZ+bbMaxZ)/2;
			double radius=Math.sqrt((bbMaxX-bbMinX)*(bbMaxX-bbMinX)+(bbMaxY-bbMinY)*(bbMaxY-bbMinY)+(bbMaxZ-bbMinZ)*(bbMaxZ-bbMinZ))/2;

			if(centerBBX>centerBBZ/2||centerBBX<-centerBBZ/2||centerBBY>centerBBZ/2||centerBBY<-centerBBZ/2)
			{
				double min= Math.min(Math.abs(-Math.sqrt(2)/2*centerBBX+Math.sqrt(2)/2*centerBBZ),
							Math.min(Math.abs(-Math.sqrt(2)/2*centerBBY+Math.sqrt(2)/2*centerBBZ),
							Math.min(Math.abs(Math.sqrt(2)/2*centerBBX+Math.sqrt(2)/2*centerBBZ),
									 Math.abs(Math.sqrt(2)/2*centerBBY+Math.sqrt(2)/2*centerBBZ))));
				if(min<-radius) continue;//outside frustum
			}

			ArrayList<triangle> draw=new ArrayList<triangle>();
			Vec3d origOne=new Vec3d(oneX,oneY,oneZ);
			Vec3d origTwo=new Vec3d(twoX,twoY,twoZ);
			Vec3d origThree=new Vec3d(threeX,threeY,threeZ);

			polygon curPolygon=new polygon(new Vec3d[]{origOne,origTwo,origThree},origOne,origTwo,origThree,null,null,null,null,0);
			curPolygon=split(curPolygon,new double[]{normalOneX,normalOneY,normalOneZ});
			if(curPolygon==null)
				continue;
			curPolygon=split(curPolygon,new double[]{normalTwoX,normalTwoY,normalTwoZ});
			if(curPolygon==null)
				continue;
			curPolygon=split(curPolygon,new double[]{normalThreeX,normalThreeY,normalThreeZ});
			if(curPolygon==null)
				continue;
			curPolygon=split(curPolygon,new double[]{normalFourX,normalFourY,normalFourZ});
			if(curPolygon==null)
				continue;
			for (int i = 1; i < curPolygon.points.length-1; i++) {
				draw.add(new triangle(origOne,origTwo,origThree,curPolygon.points[0],curPolygon.points[i],curPolygon.points[i+1],objects.get(z).surfaceNormalOne,objects.get(z).surfaceNormalTwo,objects.get(z).surfaceNormalThree));
			}
			double trueOneX=oneX;
			double trueOneY=oneY;
			double trueOneZ=oneZ;
			double trueTwoX=twoX;
			double trueTwoY=twoY;
			double trueTwoZ=twoZ;
			double trueThreeX=threeX;
			double trueThreeY=threeY;
			double trueThreeZ=threeZ;
			// double origNormalX=normalX;
			// double origNormalY=normalY;
			// double origNormalZ=normalZ;
			// double origTriangleArea=Math.sqrt(origNormalX*origNormalX+origNormalY*origNormalY+origNormalZ*origNormalZ);
			for (int i = 0; i < draw.size(); i++) {
				//what I need to do is to make the weights based off the original coords, and somehow preserve the dis to zero values for the new coords. Either that, or don't care about runtime.
				//seems like my first attempt didn't work.
				//maybe shift over to a per pixel lighting instead of per vertex lighting?
				//looks like the z interpolation had to be the inverse interpolation instead of normal interpolation
				oneX=draw.get(i).curOneX;
				oneY=draw.get(i).curOneY;
				oneZ=draw.get(i).curOneZ;
				twoX=draw.get(i).curTwoX;
				twoY=draw.get(i).curTwoY;
				twoZ=draw.get(i).curTwoZ;
				threeX=draw.get(i).curThreeX;
				threeY=draw.get(i).curThreeY;
				threeZ=draw.get(i).curThreeZ;

				double inverseOneZ=1/oneZ;
				double newOneX=oneX*inverseOneZ*640+320;
				double newOneY=oneY*inverseOneZ*640+320;

				double inverseTwoZ=1/twoZ;
				double newTwoX=twoX*inverseTwoZ*640+320;
				double newTwoY=twoY*inverseTwoZ*640+320;

				double inverseThreeZ=1/threeZ;
				double newThreeX=threeX*inverseThreeZ*640+320;
				double newThreeY=threeY*inverseThreeZ*640+320;
				// double minX=Math.min(newOneX,Math.min(newTwoX,newThreeX));
				// double maxX=Math.max(newOneX,Math.max(newTwoX,newThreeX));
				double minY=Math.min(newOneY,Math.min(newTwoY,newThreeY));
				double maxY=Math.max(newOneY,Math.max(newTwoY,newThreeY));

				double min_y=Math.max(0,minY);
				double max_y=Math.min(640,maxY);
				double denom=(newTwoY-newThreeY)*(newOneX-newThreeX)+(newThreeX-newTwoX)*(newOneY-newThreeY);
				if(denom==0) continue;
				// double inverseDenom=1/denom;
				double inverseDenom=1/denom;
				// System.out.println(denom);
				double weight_one_inc_1=(newTwoY-newThreeY)*inverseDenom;
				double weight_two_inc_1=(newThreeY-newOneY)*inverseDenom;
				double weight_three_inc_1=(newOneY-newTwoY)*inverseDenom;
				// if(weight_one_inc_1>1)
				// 	System.out.println(weight_one_inc_1);
				double inverseWeightOneInc=1/weight_one_inc_1;
				double inverseWeightTwoInc=1/weight_two_inc_1;
				double inverseWeightThreeInc=1/weight_three_inc_1;
				double weight_one_inc_2=(newThreeX-newTwoX)*inverseDenom;
				double weight_two_inc_2=(newOneX-newThreeX)*inverseDenom;
				double weight_three_inc_2=(newTwoX-newOneX)*inverseDenom;
				double disToZeroInc1=-weight_one_inc_2*inverseWeightOneInc;
				double disToZeroInc2=-weight_two_inc_2*inverseWeightTwoInc;
				double disToZeroInc3=-weight_three_inc_2*inverseWeightThreeInc;
				double interpolated_inc=weight_one_inc_1/oneZ+weight_two_inc_1/twoZ+weight_three_inc_1/threeZ;
				double interpolated_inc_2=weight_one_inc_2/oneZ+weight_two_inc_2/twoZ+weight_three_inc_2/threeZ;

				double weight_one=weight_one_inc_1*-newThreeX+weight_one_inc_2*((int)min_y-newThreeY);
				double weight_two=weight_two_inc_1*-newThreeX+weight_two_inc_2*((int)min_y-newThreeY);
				double weight_three=1-weight_one-weight_two;

				double disToZero1=-weight_one*inverseWeightOneInc;
				double disToZero2=-weight_two*inverseWeightTwoInc;
				double disToZero3=-weight_three*inverseWeightThreeInc;

				double interpolated_z_cur=weight_one/oneZ+weight_two/twoZ+weight_three/threeZ;

				boolean flag1=true;
				for (int y = (int)min_y; y < max_y; y++) {
					double inc_1=weight_one_inc_1;
					double inc_2=weight_two_inc_1;
					double inc_3=weight_three_inc_1;

					double one=Math.ceil(disToZero1);
					double two=Math.ceil(disToZero2);
					double three=Math.ceil(disToZero3);

					if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0))
					{
						// lightIntensityCurRed+=lightIntensityRedInc2;
						// lightIntensityCurGreen+=lightIntensityGreenInc2;
						// lightIntensityCurBlue+=lightIntensityBlueInc2;
						interpolated_z_cur+=interpolated_inc_2;
						// weight_one_cur+=weight_one_inc_2;
						// weight_two_cur+=weight_two_inc_2;
						// weight_three_cur+=weight_three_inc_2;
						// denomCur+=denomInc2;
						// correct_denom_cur+=correct_denom_inc;
						// correct_num_1_cur+=correct_num_1_inc;
						// correct_num_2_cur+=correct_num_2_inc;
						// textureCurX+=textureCurIncX2;
						// textureCurY+=textureCurIncY2;
						disToZero1+=disToZeroInc1;
						disToZero2+=disToZeroInc2;
						disToZero3+=disToZeroInc3;
						continue;
					}
					double interpolated_z=interpolated_z_cur;
					int min=0;
					if(inc_1>0&&one>0)
						min=Math.max(min,(int)one);
					if(inc_2>0&&two>0)
						min=Math.max(min,(int)two);
					if(inc_3>0&&three>0)
						min=Math.max(min,(int)three);
					// curTextureX+=textureCurIncX1*min;
					// curTextureY+=textureCurIncY1*min;
					// lightIntensityRed+=lightIntensityRedInc1*min;
					// lightIntensityGreen+=lightIntensityGreenInc1*min;
					// lightIntensityBlue+=lightIntensityBlueInc1*min;
					// curDenom+=denomInc1*min;
					// weight_one+=inc_1*min;
					// weight_two+=inc_2*min;
					// weight_three+=inc_3*min;
					interpolated_z+=interpolated_inc*min;
					// correct_denom+=denom_inc*min;
					// correct_num_1+=correct_num_inc_1*min;
					// correct_num_2+=correct_num_inc_2*min;
					// double disToZero1=-weight_one/inc_1;
					// double disToZero2=-weight_two/inc_2;
					// double disToZero3=-weight_three/inc_3;
					int max=640;
					// if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0)) max=0;
					if(inc_1<0)
						max=Math.min(max,(int)one);
					if(inc_2<0)
						max=Math.min(max,(int)two);
					if(inc_3<0)
						max=Math.min(max,(int)three);
					for(int x=min;x<max;x++)//this loop also takes up a lot of time
					{
						// if(x!=min&&x!=Math.floor(max)-1) continue;
						// numLoops++;
						// if(weight_one>=0&&weight_two>=0&&weight_three>=0)//this if statement block takes up a lot of time
						// {
							if(zbuffer[y*640+x]<interpolated_z)
							{
								totDrawn++;
								flag1=false;
								// num++;
								// if(!zbufferUsed[y*640+x])
									// unique++;
								zbuffer[y*640+x]=interpolated_z;
								double curPosZ=1/interpolated_z;
								double curPosX=(x-320)*curPosZ/640;
								double curPosY=(y-320)*curPosZ/640;

								double vecOneX=trueTwoX-trueOneX;
								double vecOneY=trueTwoY-trueOneY;
								double vecOneZ=trueTwoZ-trueOneZ;
								double vecTwoX=trueThreeX-trueOneX;
								double vecTwoY=trueThreeY-trueOneY;
								double vecTwoZ=trueThreeZ-trueOneZ;
								double vecThreeX=curPosX-trueOneX;
								double vecThreeY=curPosY-trueOneY;
								double vecThreeZ=curPosZ-trueOneZ;

								double dotOneOne=vecOneX*vecOneX+vecOneY*vecOneY+vecOneZ*vecOneZ;
								double dotOneTwo=vecOneX*vecTwoX+vecOneY*vecTwoY+vecOneZ*vecTwoZ;
								double dotTwoTwo=vecTwoX*vecTwoX+vecTwoY*vecTwoY+vecTwoZ*vecTwoZ;
								double dotThreeOne=vecThreeX*vecOneX+vecThreeY*vecOneY+vecThreeZ*vecOneZ;
								double dotThreeTwo=vecThreeX*vecTwoX+vecThreeY*vecTwoY+vecThreeZ*vecTwoZ;

								double denomin=dotOneOne*dotTwoTwo-dotOneTwo*dotOneTwo;
								double weightTwo=(dotTwoTwo*dotThreeOne-dotOneTwo*dotThreeTwo)/denomin;
								double weightThree=(dotOneOne*dotThreeTwo-dotOneTwo*dotThreeOne)/denomin;
								double weightOne=1-weightTwo-weightThree;

								// double curUX=trueTwoX-curPosX;
								// double curUY=trueTwoY-curPosY;
								// double curUZ=trueTwoZ-curPosZ;
								// double curVX=trueThreeX-curPosX;
								// double curVY=trueThreeY-curPosY;
								// double curVZ=trueThreeZ-curPosZ;

								// double curNormalX=curUY*curVZ-curUZ*curVY;
								// double curNormalY=curUZ*curVX-curUX*curVZ;
								// double curNormalZ=curUX*curVY-curUY*curVX;
								// double curTriangleArea=Math.sqrt(curNormalX*curNormalX+curNormalY*curNormalY+curNormalZ*curNormalZ);
								// double weightOne=curTriangleArea/origTriangleArea;

								// curUX=trueOneX-curPosX;
								// curUY=trueOneY-curPosY;
								// curUZ=trueOneZ-curPosZ;
								// // curVX=trueThreeX-curPosX;
								// // curVY=trueThreeY-curPosY;
								// // curVZ=trueThreeZ-curPosZ;

								// curNormalX=curUY*curVZ-curUZ*curVY;
								// curNormalY=curUZ*curVX-curUX*curVZ;
								// curNormalZ=curUX*curVY-curUY*curVX;
								// curTriangleArea=Math.sqrt(curNormalX*curNormalX+curNormalY*curNormalY+curNormalZ*curNormalZ);
								// double weightTwo=curTriangleArea/origTriangleArea;

								// curVX=trueTwoX-curPosX;
								// curVY=trueTwoY-curPosY;
								// curVZ=trueTwoZ-curPosZ;
								// // curVX=trueOneX-curPosX;
								// // curVY=trueOneY-curPosY;
								// // curVZ=trueOneZ-curPosZ;

								// curNormalX=curUY*curVZ-curUZ*curVY;
								// curNormalY=curUZ*curVX-curUX*curVZ;
								// curNormalZ=curUX*curVY-curUY*curVX;
								// curTriangleArea=Math.sqrt(curNormalX*curNormalX+curNormalY*curNormalY+curNormalZ*curNormalZ);
								// double weightThree=curTriangleArea/origTriangleArea;

								// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)twoTriangleArea)/2);
								// approx=approx*(3-approx*approx*twoTriangleArea)/2;
								// twoTriangleArea=approx;
								// pixels[y*640+x]=0;
								double surfaceNormalX=objects.get(z).surfaceNormalOne.avgNormX*weightOne+objects.get(z).surfaceNormalTwo.avgNormX*weightTwo+objects.get(z).surfaceNormalThree.avgNormX*weightThree;
								double surfaceNormalY=objects.get(z).surfaceNormalOne.avgNormY*weightOne+objects.get(z).surfaceNormalTwo.avgNormY*weightTwo+objects.get(z).surfaceNormalThree.avgNormY*weightThree;
								double surfaceNormalZ=objects.get(z).surfaceNormalOne.avgNormZ*weightOne+objects.get(z).surfaceNormalTwo.avgNormZ*weightTwo+objects.get(z).surfaceNormalThree.avgNormZ*weightThree;
								double dis=surfaceNormalX*surfaceNormalX+surfaceNormalY*surfaceNormalY+surfaceNormalZ*surfaceNormalZ;
								double approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)dis)/2);
								approx=approx*(3-approx*approx*dis)/2;
								surfaceNormalX*=approx;
								surfaceNormalY*=approx;
								surfaceNormalZ*=approx;
								double lightVectorX=screen.light.x1-curPosX;
								double lightVectorY=screen.light.y1-curPosY;
								double lightVectorZ=screen.light.z1-curPosZ;
								double lightVectorDis=lightVectorX*lightVectorX+lightVectorY*lightVectorY+lightVectorZ*lightVectorZ;
								approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorDis)/2);
								approx=approx*(3-approx*approx*lightVectorDis)/2;
								lightVectorX*=approx;
								lightVectorY*=approx;
								lightVectorZ*=approx;
								double diffusion=lightVectorX*surfaceNormalX+lightVectorY*surfaceNormalY+lightVectorZ*surfaceNormalZ;
								if(diffusion<0)
									diffusion=0;
								double redIntensity=objects.get(z).shade.ambientRed+diffusion*objects.get(z).shade.diffusionRed;
								double greenIntensity=objects.get(z).shade.ambientGreen+diffusion*objects.get(z).shade.diffusionGreen;
								double blueIntensity=objects.get(z).shade.ambientBlue+diffusion*objects.get(z).shade.diffusionBlue;
								redIntensity*=4;
								greenIntensity*=4;
								blueIntensity*=4;
								// double redIntensity=objects.get(z).lightIntensityOneRed*weightOne+objects.get(z).lightIntensityTwoRed*weightTwo+objects.get(z).lightIntensityThreeRed*weightThree;
								// double greenIntensity=objects.get(z).lightIntensityOneGreen*weightOne+objects.get(z).lightIntensityTwoGreen*weightTwo+objects.get(z).lightIntensityThreeGreen*weightThree;
								// double blueIntensity=objects.get(z).lightIntensityOneBlue*weightOne+objects.get(z).lightIntensityTwoBlue*weightTwo+objects.get(z).lightIntensityThreeBlue*weightThree;
								//research more on fixing the texture calculation
								double uCoordOne=objects.get(z).uCoordOne;
								double uCoordTwo=objects.get(z).uCoordTwo;
								double uCoordThree=objects.get(z).uCoordThree;
								double vCoordOne=objects.get(z).vCoordOne;
								double vCoordTwo=objects.get(z).vCoordTwo;
								double vCoordThree=objects.get(z).vCoordThree;

								double curTextureX=uCoordOne*weightOne+uCoordTwo*weightTwo+uCoordThree*weightThree;
								double curTextureY=vCoordOne*weightOne+vCoordTwo*weightTwo+vCoordThree*weightThree;

								if(redIntensity<0)
									redIntensity=0;
								if(greenIntensity<0)
									greenIntensity=0;
								if(blueIntensity<0)
									blueIntensity=0;
								HOMMaps[7][y][x]=1;
								if(objects.get(z).pixels!=null)
								{
									double[] colors=getTextureColor(curTextureX,curTextureY,objects.get(z).pixels);
									if(colors==null) continue;
									colors[0]*=redIntensity;
									colors[1]*=greenIntensity;
									colors[2]*=blueIntensity;
									for (int j = 0; j < colors.length; j++) {
										if(colors[j]>255) colors[j]=255;
									}
									int red=(int)(colors[0]*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]>>16&255));
									int green=(int)(colors[1]*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]>>8&255));
									int blue=(int)(colors[2]*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]&255));
									red=red<<16;
									green=green<<8;
									pixels[y*640+x]=red|green|blue;
								}
								else
								{
									int resRed=(int)(redIntensity*255);
									int resGreen=(int)(greenIntensity*255);
									int resBlue=(int)(blueIntensity*255);
									if(resRed>255)
										resRed=255;
									if(resGreen>255)
										resGreen=255;
									if(resBlue>255)
										resBlue=255;
									int red=(int)(resRed*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]>>16&255));
									int green=(int)(resGreen*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]>>8&255));
									int blue=(int)(resBlue*objects.get(z).shade.transparent+(1-objects.get(z).shade.transparent)*(pixels[y*640+x]&255));
									red=red<<16;
									green=green<<8;
									pixels[y*640+x]=red|green|blue;
								}
							}
						// }
						// else if(found) break;
						// curDenom+=denomInc1;
						// curTextureX+=textureCurIncX1;
						// curTextureY+=textureCurIncY1;
						// lightIntensityRed+=lightIntensityRedInc1;
						// lightIntensityGreen+=lightIntensityGreenInc1;
						// lightIntensityBlue+=lightIntensityBlueInc1;
						// weight_one+=inc_1;
						// weight_two+=inc_2;
						// weight_three+=inc_3;
						interpolated_z+=interpolated_inc;
						// correct_denom+=denom_inc;
						// correct_num_1+=correct_num_inc_1;
						// correct_num_2+=correct_num_inc_2;
					}
					// denomCur+=denomInc2;
					// textureCurX+=textureCurIncX2;
					// textureCurY+=textureCurIncY2;
					// lightIntensityCurRed+=lightIntensityRedInc2;
					// lightIntensityCurGreen+=lightIntensityGreenInc2;
					// lightIntensityCurBlue+=lightIntensityBlueInc2;
					// weight_one_cur+=weight_one_inc_2;
					// weight_two_cur+=weight_two_inc_2;
					// weight_three_cur+=weight_three_inc_2;
					interpolated_z_cur+=interpolated_inc_2;
					// correct_denom_cur+=correct_denom_inc;
					// correct_num_1_cur+=correct_num_1_inc;
					// correct_num_2_cur+=correct_num_2_inc;
					disToZero1+=disToZeroInc1;
					disToZero2+=disToZeroInc2;
					disToZero3+=disToZeroInc3;
				}
			}
			// if(!draw.isEmpty()&&newOneX>=0&&newOneX<640&&newOneY>=0&&newOneY<640)
			// 	pixels[(int)newOneY*640+(int)newOneX]=new Color(0,255,0).getRGB();
			
			// numOccluded+=flag1?1:0;
		}
		int maxY=320;
		int maxX=320;
		for (int i = 6; i >= 0; i--) {
			for (int y = 0; y < maxY; y++) {
				for (int x = 0; x < maxX; x++) {
					HOMMaps[i][y][x]=(HOMMaps[i+1][y*2][x*2]+HOMMaps[i+1][y*2][x*2+1]+HOMMaps[i+1][y*2+1][x*2]+HOMMaps[i+1][y*2+1][x*2+1])/4;
				}
			}
			maxY/=2;
			maxX/=2;
		}
		int size=320;
		int offSet=0;
		for (int a = 7 - 1; a >= 0; a--) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					int red=(int)((1-HOMMaps[a][i][j])*255)<<16;
					int green=(int)((1-HOMMaps[a][i][j])*255)<<8;
					int blue=(int)((1-HOMMaps[a][i][j])*255);
					pixels[(i+offSet)*640+j]=red|green|blue;
				}
			}
			offSet+=size;
			size/=2;
		}
		// occluders.clear();
		// render(BSPTree,pixels);
		// if(numCulled>prevCull+1000)
		// {
		// 	for(object a:pendingOccluders)
		// 	{
		// 		occluders.add(a);
		// 		if(occluders.size()>=numOfOccluders)
		// 			occluders.poll();
		// 	}
		// }
		pendingOccluders=new ArrayList<object>();
		// if(numCulled<100000)
		// {
		// 	numOfOccluders+=100;
		// }
		// if(numCulled>90000&&(System.nanoTime()-timeCount)>60000000)
		// {
		// 	numOfOccluders-=100;
		// }
		// if(numCulled<100000&&(System.nanoTime()-timeCount)>60000000)
		// {
		// 	//look at the lowest resolution map and try to find polygons that would occlude that area.
		// 	// System.out.println(Arrays.deepToString(HOMMaps[7]));
		// 	ArrayList<object> unusedTriangles=new ArrayList<object>();
		// 	for (int i = 0; i < objects.size(); i++) {
		// 		if(!used[objects.get(i).ID])
		// 			unusedTriangles.add(objects.get(i));
		// 	}
		// 	boolean[][] HOMPixel=new boolean[5][5];
		// 	ArrayList<rectangle> lowValGroups=new ArrayList<rectangle>();
		// 	for (int i = 0; i < 5; i++) {
		// 		for (int j = 0; j < 5; j++) {
		// 			if(HOMMaps[0][i][j]<1E-9)//too low
		// 			{
		// 				HOMPixel[i][j]=true;
		// 			}
		// 		}
		// 	}

		// 	for (int i = 0; i < 5; i++) {
		// 		for (int j = 0; j < 5; j++) {
		// 			if(HOMPixel[i][j])
		// 			{
		// 				int maxRowVal=i;
		// 				for (int a = i+1; a < 5; a++) {
		// 					if(!HOMPixel[a][j]) break;
		// 					maxRowVal=a;
		// 				}
		// 				int maxColVal=j;
		// 				outer: for (int a = j+1; a < 5; a++) {
		// 					for (int b = i; b <= maxRowVal; b++) {
		// 						if(!HOMPixel[a][b]) break;
		// 						maxColVal=a;
		// 					}
		// 				}
		// 				for (int a = i; a <= maxRowVal; a++) {
		// 					for (int b = j; b <= maxColVal; b++) {
		// 						HOMPixel[a][b]=false;
		// 					}
		// 				}
		// 				lowValGroups.add(new rectangle(j,maxColVal,i,maxRowVal));
		// 			}
		// 		}
		// 	}
		// 	System.out.println(lowValGroups);
		// 	double min=10;
		// 	rectangle lowest=null;
		// 	for (int i = 0; i < lowValGroups.size(); i++) {
		// 		double curAvg=0;
		// 		int size=0;
		// 		for (int a = lowValGroups.get(i).lowY; a <= lowValGroups.get(i).highY; a++) {
		// 			for (int b = lowValGroups.get(i).lowX; b <= lowValGroups.get(i).highX; b++) {
		// 				curAvg+=HOMMaps[0][a][b];
		// 				size++;
		// 			}
		// 		}
		// 		curAvg/=size;
		// 		if(min>curAvg)
		// 		{
		// 			min=curAvg;
		// 			lowest=lowValGroups.get(i);
		// 		}
		// 	}
		// 	if(lowest!=null)
		// 	{
		// 		int lowerYBound=lowest.lowY<<7;
		// 		int higherYBound=lowest.highY<<7;
		// 		int lowerXBound=lowest.lowX<<7;
		// 		int higherXBound=lowest.highX<<7;
		// 		Collections.sort(unusedTriangles);
		// 		for (int i = 0; i < unusedTriangles.size(); i++) {
		// 			double centerNewX=unusedTriangles.get(i).rotatedX/unusedTriangles.get(i).rotatedZ;//in screen if within -.5 and .5
		// 			double centerNewY=unusedTriangles.get(i).rotatedY/unusedTriangles.get(i).rotatedZ;//in screen if within -.5 and .5
		// 			if(centerNewY+10<lowerYBound||centerNewY-10>higherYBound||centerNewX+10<lowerXBound||centerNewX-10>higherXBound) continue;
		// 			if(pendingOccluders.size()>=1000) break;
		// 			pendingOccluders.add(unusedTriangles.get(i));
		// 			// used[i]=true;
		// 		}
		// 	}
		// 	System.out.println(lowest);
		// }
		if(key_listener.change1)
			for (int i = 0; i < 640; i++) {
				for (int j = 0; j < 640; j++) {
					double temp=HOMMaps[7][i][j]*255;
					int red=(int)temp<<16;
					int green=(int)temp<<8;
					int blue=(int)temp;
					pixels[i*640+j]=red+green+blue;
				}
			}
		prevCull=numCulled;
		// System.out.println(Arrays.toString(pixelsDrawn.arr[320]));
		// System.out.println("total occluded: "+totOccluded+" total triangles: "+totTriangles);
		// System.out.println("number not drawn: "+totNoDraw);
		// System.out.println("max number of vertices: "+maxNumOfVert);
		// System.out.println("Number culled: "+numCulled);
		// System.out.println("Number of occluders: "+numOfOccluders);
		fps=fps*.95+(System.nanoTime()-timeCount)*.05;
		// System.out.println("number of loops: "+numLoops+" num: "+num+" unique: "+unique);
		// System.out.println("number of occluded: "+numOccluded);
		System.out.println("render time: "+(System.nanoTime()-timeCount));
		System.out.println("fps: "+(1e9/fps));
		System.out.println("total pixels drawn: "+totDrawn);
		System.out.println();
	    return pixels;
	}
	public double[] getTextureColor(double xPos,double yPos,int[][] arr)
	{
		int xOne=(int)xPos;
		int xTwo=(int)xPos+1;
		int yOne=(int)yPos;
		int yTwo=(int)yPos+1;
		if(xPos<0||yPos<0)
		{
			double[] resColors=new double[3];
			resColors[0]=arr[0][0]>>16&255;
			resColors[1]=arr[0][0]>>8&255;
			resColors[2]=arr[0][0]&255;
			return resColors;
		}
		if(xTwo>=arr[0].length||yTwo>=arr.length)
		{
			if(xOne>=arr[0].length||yOne>=arr.length) return null;
			double[] resColors=new double[3];
			resColors[0]=arr[yOne][xOne]>>16&255;
			resColors[1]=arr[yOne][xOne]>>8&255;
			resColors[2]=arr[yOne][xOne]&255;
			return resColors;
		}
		double xDiffOne=xPos-xOne;
		double xDiffTwo=1-xPos+xOne;
		double yDiffOne=yPos-yOne;
		double yDiffTwo=1-yPos+yOne;
		double[] vals=new double[4];
		vals[0]=xDiffOne*yDiffOne;
		vals[1]=xDiffOne*yDiffTwo;
		vals[2]=xDiffTwo*yDiffOne;
		vals[3]=xDiffTwo*yDiffTwo;
		int pos=-1;
		double min=1<<20;
		for (int i = 0; i < vals.length; i++) {
			if(vals[i]<min)
			{
				min=vals[i];
				pos=i;
			}
		}
		double[] resColors=new double[3];
		if(pos==0)
		{
			resColors[0]=arr[yOne][xOne]>>16&255;
			resColors[1]=arr[yOne][xOne]>>8&255;
			resColors[2]=arr[yOne][xOne]&255;
		}
		if(pos==1)
		{
			resColors[0]=arr[yTwo][xOne]>>16&255;
			resColors[1]=arr[yTwo][xOne]>>8&255;
			resColors[2]=arr[yTwo][xOne]&255;
		}
		if(pos==2)
		{
			resColors[0]=arr[yOne][xTwo]>>16&255;
			resColors[1]=arr[yOne][xTwo]>>8&255;
			resColors[2]=arr[yOne][xTwo]&255;
		}
		if(pos==3)
		{
			resColors[0]=arr[yTwo][xTwo]>>16&255;
			resColors[1]=arr[yTwo][xTwo]>>8&255;
			resColors[2]=arr[yTwo][xTwo]&255;
		}
		// int[][] colors=new int[3][4];
		// for (int i = 0; i < 3; i++) {
		// 	Arrays.fill(colors[i],-1);
		// }
		// if(xOne>=0&&xOne<arr[0].length&&yOne>=0&&yOne<arr.length)
		// {
		// 	colors[0][0]=arr[yOne][xOne]>>16&255;
		// 	colors[1][0]=arr[yOne][xOne]>>8&255;
		// 	colors[2][0]=arr[yOne][xOne]&255;
		// }
		// if(xOne>=0&&xOne<arr[0].length&&yTwo>=0&&yTwo<arr.length)
		// {
		// 	colors[0][1]=arr[yTwo][xOne]>>16&255;
		// 	colors[1][1]=arr[yTwo][xOne]>>8&255;
		// 	colors[2][1]=arr[yTwo][xOne]&255;
		// }
		// if(xTwo>=0&&xTwo<arr[0].length&&yOne>=0&&yOne<arr.length)
		// {
		// 	colors[0][2]=arr[yOne][xTwo]>>16&255;
		// 	colors[1][2]=arr[yOne][xTwo]>>8&255;
		// 	colors[2][2]=arr[yOne][xTwo]&255;
		// }
		// if(xTwo>=0&&xTwo<arr[0].length&&yTwo>=0&&yTwo<arr.length)
		// {
		// 	colors[0][3]=arr[yTwo][xTwo]>>16&255;
		// 	colors[1][3]=arr[yTwo][xTwo]>>8&255;
		// 	colors[2][3]=arr[yTwo][xTwo]&255;
		// }
		// double[] resColors=new double[3];
		// // int[] nums=new int[3];
		// for (int i = 0; i < 3; i++) {
		// 	for (int j = 0; j < 4; j++) {
		// 		if(colors[i][j]==-1)
		// 			continue;
		// 		if(j==0)
		// 			resColors[i]+=colors[i][j]*(xPos-xOne)*(yPos-yOne);
		// 		if(j==1)
		// 			resColors[i]+=colors[i][j]*(xPos-xOne)*(yTwo-yPos);
		// 		if(j==2)
		// 			resColors[i]+=colors[i][j]*(xTwo-xPos)*(yPos-yOne);
		// 		if(j==3)
		// 			resColors[i]+=colors[i][j]*(xTwo-xPos)*(yTwo-yPos);
		// 	}
		// }
		return resColors;
	}
	public void render(binarySpacePartitionTree cur,int[] pixels)
	{
		if(cur==null||cur.drawTriangle==null) return;
		Vec3d cameraPos=new Vec3d(camera_x,camera_y,camera_z);
		if(cur.classifyPoint(cur.current[0],cameraPos)==1)
			render(cur.inFront,pixels);
		else
			render(cur.behind,pixels);
		// if(numOfTriangles>key_listener.limit) return;
		int numOfThings=0;
		int num=0;
		int unique=0;
		int numLoops=0;
		int numOccluded=0;
		int noDraw=0;
		draw: for(int i=0;i<cur.drawTriangle.size();i++)
		{
			ArrayList<triangle> tempList=cur.drawTriangle.get(i);
			polygon curPolygon=cur.current[i];
			totTriangles+=tempList.size();
			maxNumOfVert=Math.max(maxNumOfVert,curPolygon.points.length);
			// double surfaceNormX=curPolygon.polygonSurfaceNorm.avgNormX;
			// double surfaceNormY=curPolygon.polygonSurfaceNorm.avgNormY;
			// double surfaceNormZ=curPolygon.polygonSurfaceNorm.avgNormZ;

			double surfaceNormX=cosYrot*(curPolygon.polygonSurfaceNorm.avgNormX)-sinYrot*(curPolygon.polygonSurfaceNorm.avgNormZ);
			double surfaceNormY=sinXrotCosYrot*(curPolygon.polygonSurfaceNorm.avgNormZ)+sinXrotSinYrot*(curPolygon.polygonSurfaceNorm.avgNormX)+cosXrot*(curPolygon.polygonSurfaceNorm.avgNormY);
			double surfaceNormZ=cosXrotCosYrot*(curPolygon.polygonSurfaceNorm.avgNormZ)+cosXrotSinYrot*(curPolygon.polygonSurfaceNorm.avgNormX)-sinXrot*(curPolygon.polygonSurfaceNorm.avgNormY);
			double polygonPointX=cosYrot*(curPolygon.points[0].one-camera_x)-sinYrot*(curPolygon.points[0].three-camera_z);
			double polygonPointY=sinXrotCosYrot*(curPolygon.points[0].three-camera_z)+sinXrotSinYrot*(curPolygon.points[0].one-camera_x)+cosXrot*(curPolygon.points[0].two-camera_y);
			double polygonPointZ=cosXrotCosYrot*(curPolygon.points[0].three-camera_z)+cosXrotSinYrot*(curPolygon.points[0].one-camera_x)-sinXrot*(curPolygon.points[0].two-camera_y);
			if(polygonPointX*surfaceNormX+polygonPointY*surfaceNormY+polygonPointZ*surfaceNormZ<0)
			{
				totOccluded+=tempList.size();
				continue;
			}

			double oneX=cosYrot*(curPolygon.points[0].one-camera_x)-sinYrot*(curPolygon.points[0].three-camera_z);
			double oneY=sinXrotCosYrot*(curPolygon.points[0].three-camera_z)+sinXrotSinYrot*(curPolygon.points[0].one-camera_x)+cosXrot*(curPolygon.points[0].two-camera_y);
			double oneZ=cosXrotCosYrot*(curPolygon.points[0].three-camera_z)+cosXrotSinYrot*(curPolygon.points[0].one-camera_x)-sinXrot*(curPolygon.points[0].two-camera_y);
			if(oneZ<0)
			{
				totOccluded+=tempList.size();
				continue;
			}

			double minYBound=639;
			double minXBound=639;
			double maxYBound=0;
			double maxXBound=0;
			// for (int j = 0; j < curPolygon.boundingBox.length; j++) {
			// 	double rotX=cosYrot*(curPolygon.boundingBox[j].one-camera_x)-sinYrot*(curPolygon.boundingBox[j].three-camera_z);
			// 	double rotY=sinXrotCosYrot*(curPolygon.boundingBox[j].three-camera_z)+sinXrotSinYrot*(curPolygon.boundingBox[j].one-camera_x)+cosXrot*(curPolygon.boundingBox[j].two-camera_y);
			// 	double rotZ=cosXrotCosYrot*(curPolygon.boundingBox[j].three-camera_z)+cosXrotSinYrot*(curPolygon.boundingBox[j].one-camera_x)-sinXrot*(curPolygon.boundingBox[j].two-camera_y);
			// 	double newX=rotX/rotZ*640+320;
			// 	double newY=rotY/rotZ*640+320;
			// 	minYBound=Math.min(minYBound,newY);
			// 	minXBound=Math.min(minXBound,newX);
			// 	maxYBound=Math.max(maxYBound,newY);
			// 	maxXBound=Math.max(maxXBound,newX);
			// }
			maxYBound=Math.min(639,maxYBound);
			maxXBound=Math.min(639,maxXBound);
			minYBound=Math.max(0,minYBound);
			minXBound=Math.max(0,minXBound);
			// if(!occluders.contains(curPolygon.ID))
			// {
			// 	boolean isOccluded=true;
			// 	int LBound=(int)(minXBound/(1<<7));
			// 	int RBound=(int)(maxXBound/(1<<7));
			// 	int UBound=(int)(minYBound/(1<<7));
			// 	int DBound=(int)(maxYBound/(1<<7));
			// 	for (int y = UBound; y <= DBound; y++) {
			// 		for (int x = LBound; x <= RBound; x++) {
			// 			boolean flag=recurse(0,x,y,(int)minXBound,(int)maxXBound,(int)minYBound,(int)maxYBound);
			// 			isOccluded&=flag;
			// 		}
			// 	}
			// 	if(isOccluded)
			// 	{
			// 		numCulled++;
			// 		continue;
			// 	}
			// }
				// for (int j = 0; j < 8; j++) {
				// 	int LBound=(int)(minXBound/(1<<7-j));
				// 	int RBound=(int)(maxXBound/(1<<7-j));
				// 	int UBound=(int)(minYBound/(1<<7-j));
				// 	int DBound=(int)(maxYBound/(1<<7-j));
				// 	boolean allOpaque=true;
				// 	boolean allNotOpaque=true;
				// 	for (int y = UBound; y <= DBound; y++) {
				// 		for (int x = LBound; x <= RBound; x++) {
				// 			if(HOMMaps[j][y][x]<1-1E-9)
				// 				allOpaque=false;
				// 			if(HOMMaps[j][y][x]>1E-9)
				// 				allNotOpaque=false;
				// 		}
				// 	}
				// 	if(allOpaque)
				// 	{
				// 		numCulled++;
				// 		continue draw;
				// 	}
				// 	if(allNotOpaque) break;
				// }
			// if(pixelsDrawn.queryY(0,0,639,(int)minYBound,(int)maxYBound,(int)minXBound,(int)maxXBound))
			// {
			// 	totOccluded+=tempList.size();
			// 	continue;
			// }

			double surfaceNormalOneX=cosYrot*(tempList.get(0).surfaceNormalOne.avgNormX)-sinYrot*(tempList.get(0).surfaceNormalOne.avgNormZ);
			double surfaceNormalOneY=sinXrotCosYrot*(tempList.get(0).surfaceNormalOne.avgNormZ)+sinXrotSinYrot*(tempList.get(0).surfaceNormalOne.avgNormX)+cosXrot*(tempList.get(0).surfaceNormalOne.avgNormY);
			double surfaceNormalOneZ=cosXrotCosYrot*(tempList.get(0).surfaceNormalOne.avgNormZ)+cosXrotSinYrot*(tempList.get(0).surfaceNormalOne.avgNormX)-sinXrot*(tempList.get(0).surfaceNormalOne.avgNormY);
			double surfaceNormalTwoX=cosYrot*(tempList.get(0).surfaceNormalTwo.avgNormX)-sinYrot*(tempList.get(0).surfaceNormalTwo.avgNormZ);
			double surfaceNormalTwoY=sinXrotCosYrot*(tempList.get(0).surfaceNormalTwo.avgNormZ)+sinXrotSinYrot*(tempList.get(0).surfaceNormalTwo.avgNormX)+cosXrot*(tempList.get(0).surfaceNormalTwo.avgNormY);
			double surfaceNormalTwoZ=cosXrotCosYrot*(tempList.get(0).surfaceNormalTwo.avgNormZ)+cosXrotSinYrot*(tempList.get(0).surfaceNormalTwo.avgNormX)-sinXrot*(tempList.get(0).surfaceNormalTwo.avgNormY);
			double inverseOneZ=1/oneZ;
			double newOneX=oneX*inverseOneZ*640+320;
			double newOneY=oneY*inverseOneZ*640+320;
			double twoX=cosYrot*(curPolygon.points[1].one-camera_x)-sinYrot*(curPolygon.points[1].three-camera_z);
			double twoY=sinXrotCosYrot*(curPolygon.points[1].three-camera_z)+sinXrotSinYrot*(curPolygon.points[1].one-camera_x)+cosXrot*(curPolygon.points[1].two-camera_y);
			double twoZ=cosXrotCosYrot*(curPolygon.points[1].three-camera_z)+cosXrotSinYrot*(curPolygon.points[1].one-camera_x)-sinXrot*(curPolygon.points[1].two-camera_y);
			double inverseTwoZ=1/twoZ;
			double newTwoX=twoX*inverseTwoZ*640+320;
			double newTwoY=twoY*inverseTwoZ*640+320;
			double lightVectorOneX=rotLightX-oneX;
			double lightVectorOneY=rotLightY-oneY;
			double lightVectorOneZ=rotLightZ-oneZ;
			double lightVectorOneDis=lightVectorOneX*lightVectorOneX+lightVectorOneY*lightVectorOneY+lightVectorOneZ*lightVectorOneZ;
			double approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorOneDis)/2);
			approx=approx*(3-approx*approx*lightVectorOneDis)/2;
			lightVectorOneX*=approx;
			lightVectorOneY*=approx;
			lightVectorOneZ*=approx;
			double diffusionOne=lightVectorOneX*surfaceNormalOneX+lightVectorOneY*surfaceNormalOneY+lightVectorOneZ*surfaceNormalOneZ;
			double reflectOne=0;
			double lightIntensityOne=diffusionOne*255+reflectOne*255;
			double lightVectorTwoX=rotLightX-twoX;
			double lightVectorTwoY=rotLightY-twoY;
			double lightVectorTwoZ=rotLightZ-twoZ;
			double lightVectorTwoDis=lightVectorTwoX*lightVectorTwoX+lightVectorTwoY*lightVectorTwoY+lightVectorTwoZ*lightVectorTwoZ;
			approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorTwoDis)/2);
			approx=approx*(3-approx*approx*lightVectorTwoDis)/2;
			lightVectorTwoX*=approx;
			lightVectorTwoY*=approx;
			lightVectorTwoZ*=approx;
			double diffusionTwo=lightVectorTwoX*surfaceNormalTwoX+lightVectorTwoY*surfaceNormalTwoY+lightVectorTwoZ*surfaceNormalTwoZ;
			double reflectTwo=0;
			double lightIntensityTwo=diffusionTwo*255+reflectTwo*255;

			boolean flag1=true;
			// if(twoZ<0) continue;
			numOfTriangles++;
			for(int z=0;z<tempList.size();z++)
			{
				triangle currentTriangle=tempList.get(z);
				double threeX=cosYrot*(currentTriangle.curThreeX-camera_x)-sinYrot*(currentTriangle.curThreeZ-camera_z);
				double threeY=sinXrotCosYrot*(currentTriangle.curThreeZ-camera_z)+sinXrotSinYrot*(currentTriangle.curThreeX-camera_x)+cosXrot*(currentTriangle.curThreeY-camera_y);
				double threeZ=cosXrotCosYrot*(currentTriangle.curThreeZ-camera_z)+cosXrotSinYrot*(currentTriangle.curThreeX-camera_x)-sinXrot*(currentTriangle.curThreeY-camera_y);
				// if(threeZ<0) continue;

				double inverseThreeZ=1/threeZ;
				double newThreeX=threeX*inverseThreeZ*640+320;
				double newThreeY=threeY*inverseThreeZ*640+320;

				double minX=Math.min(newOneX,Math.min(newTwoX,newThreeX));
				double maxX=Math.max(newOneX,Math.max(newTwoX,newThreeX));
				double minY=Math.min(newOneY,Math.min(newTwoY,newThreeY));
				double maxY=Math.max(newOneY,Math.max(newTwoY,newThreeY));

				double surfaceNormalThreeX=cosYrot*(currentTriangle.surfaceNormalThree.avgNormX)-sinYrot*(currentTriangle.surfaceNormalThree.avgNormZ);
				double surfaceNormalThreeY=sinXrotCosYrot*(currentTriangle.surfaceNormalThree.avgNormZ)+sinXrotSinYrot*(currentTriangle.surfaceNormalThree.avgNormX)+cosXrot*(currentTriangle.surfaceNormalThree.avgNormY);
				double surfaceNormalThreeZ=cosXrotCosYrot*(currentTriangle.surfaceNormalThree.avgNormZ)+cosXrotSinYrot*(currentTriangle.surfaceNormalThree.avgNormX)-sinXrot*(currentTriangle.surfaceNormalThree.avgNormY);

				minX=Math.max(0,minX);
				minY=Math.max(0,minY);
				maxX=Math.min(640,maxX);
				maxY=Math.min(640,maxY);
				// if(pixelsDrawn.queryY(0,0,639,(int)minY,(int)maxY,(int)minX,(int)maxX))
				// {
				// 	numOccluded++;
				// 	continue;
				// }

				// double reflectVectorOneX=2*(lightVectorOneX*surfaceNormalOneX+lightVectorOneY*surfaceNormalOneY+lightVectorOneZ*surfaceNormalOneZ)*surfaceNormalOneX-lightVectorOneX;
				// double reflectVectorOneY=2*(lightVectorOneX*surfaceNormalOneX+lightVectorOneY*surfaceNormalOneY+lightVectorOneZ*surfaceNormalOneZ)*surfaceNormalOneY-lightVectorOneY;
				// double reflectVectorOneZ=2*(lightVectorOneX*surfaceNormalOneX+lightVectorOneY*surfaceNormalOneY+lightVectorOneZ*surfaceNormalOneZ)*surfaceNormalOneZ-lightVectorOneZ;
				// double cameraVectorOneX=0-oneX;
				// double cameraVectorOneY=0-oneY;
				// double cameraVectorOneZ=0-oneZ;
				// double cameraVectorOneDis=cameraVectorOneX*cameraVectorOneX+cameraVectorOneY*cameraVectorOneY+cameraVectorOneZ*cameraVectorOneZ;
				// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)cameraVectorOneDis)/2);
				// approx=approx*(3-approx*approx*lightVectorOneDis)/2;
				// cameraVectorOneX*=approx;
				// cameraVectorOneY*=approx;
				// cameraVectorOneZ*=approx;

				// double reflectOne=reflectVectorOneX*cameraVectorOneX+reflectVectorOneY*cameraVectorOneY+reflectVectorOneZ*cameraVectorOneZ;

				

				// double reflectVectorTwoX=2*(lightVectorTwoX*surfaceNormalTwoX+lightVectorTwoY*surfaceNormalTwoY+lightVectorTwoZ*surfaceNormalTwoZ)*surfaceNormalTwoX-lightVectorTwoX;
				// double reflectVectorTwoY=2*(lightVectorTwoX*surfaceNormalTwoX+lightVectorTwoY*surfaceNormalTwoY+lightVectorTwoZ*surfaceNormalTwoZ)*surfaceNormalTwoY-lightVectorTwoY;
				// double reflectVectorTwoZ=2*(lightVectorTwoX*surfaceNormalTwoX+lightVectorTwoY*surfaceNormalTwoY+lightVectorTwoZ*surfaceNormalTwoZ)*surfaceNormalTwoZ-lightVectorTwoZ;
				// double cameraVectorTwoX=0-twoX;
				// double cameraVectorTwoY=0-twoY;
				// double cameraVectorTwoZ=0-twoZ;
				// double cameraVectorTwoDis=cameraVectorTwoX*cameraVectorTwoX+cameraVectorTwoY*cameraVectorTwoY+cameraVectorTwoZ*cameraVectorTwoZ;
				// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)cameraVectorTwoDis)/2);
				// approx=approx*(3-approx*approx*lightVectorTwoDis)/2;
				// cameraVectorTwoX*=approx;
				// cameraVectorTwoY*=approx;
				// cameraVectorTwoZ*=approx;

				// double reflectTwo=reflectVectorTwoX*cameraVectorTwoX+reflectVectorTwoY*cameraVectorTwoY+reflectVectorTwoZ*cameraVectorTwoZ;

				double lightVectorThreeX=rotLightX-threeX;
				double lightVectorThreeY=rotLightY-threeY;
				double lightVectorThreeZ=rotLightZ-threeZ;
				double lightVectorThreeDis=lightVectorThreeX*lightVectorThreeX+lightVectorThreeY*lightVectorThreeY+lightVectorThreeZ*lightVectorThreeZ;
				approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)lightVectorThreeDis)/2);
				approx=approx*(3-approx*approx*lightVectorThreeDis)/2;
				lightVectorThreeX*=approx;
				lightVectorThreeY*=approx;
				lightVectorThreeZ*=approx;
				double diffusionThree=lightVectorThreeX*surfaceNormalThreeX+lightVectorThreeY*surfaceNormalThreeY+lightVectorThreeZ*surfaceNormalThreeZ;

				// double reflectVectorThreeX=2*(lightVectorThreeX*surfaceNormalThreeX+lightVectorThreeY*surfaceNormalThreeY+lightVectorThreeZ*surfaceNormalThreeZ)*surfaceNormalThreeX-lightVectorThreeX;
				// double reflectVectorThreeY=2*(lightVectorThreeX*surfaceNormalThreeX+lightVectorThreeY*surfaceNormalThreeY+lightVectorThreeZ*surfaceNormalThreeZ)*surfaceNormalThreeY-lightVectorThreeY;
				// double reflectVectorThreeZ=2*(lightVectorThreeX*surfaceNormalThreeX+lightVectorThreeY*surfaceNormalThreeY+lightVectorThreeZ*surfaceNormalThreeZ)*surfaceNormalThreeZ-lightVectorThreeZ;
				// double cameraVectorThreeX=0-threeX;
				// double cameraVectorThreeY=0-threeY;
				// double cameraVectorThreeZ=0-threeZ;
				// double cameraVectorThreeDis=cameraVectorThreeX*cameraVectorThreeX+cameraVectorThreeY*cameraVectorThreeY+cameraVectorThreeZ*cameraVectorThreeZ;
				// approx=Float.intBitsToFloat(0x5F3759DF-Float.floatToIntBits((float)cameraVectorThreeDis)/2);
				// approx=approx*(3-approx*approx*lightVectorThreeDis)/2;
				// cameraVectorThreeX*=approx;
				// cameraVectorThreeY*=approx;
				// cameraVectorThreeZ*=approx;

				// double reflectThree=reflectVectorThreeX*cameraVectorThreeX+reflectVectorThreeY*cameraVectorThreeY+reflectVectorThreeZ*cameraVectorThreeZ;
				double reflectThree=0;
				double lightIntensityThree=diffusionThree*255+reflectThree*255;

				if(twoZ<0||threeZ<0)
				{
					twoX=threeX;
					twoY=threeY;
					twoZ=threeZ;
					surfaceNormalTwoX=surfaceNormalThreeX;
					surfaceNormalTwoY=surfaceNormalThreeY;
					surfaceNormalTwoZ=surfaceNormalThreeZ;
					lightIntensityTwo=lightIntensityThree;
					newTwoX=newThreeX;
					newTwoY=newThreeY;
					continue;
				}

				double denom=(newTwoY-newThreeY)*(newOneX-newThreeX)+(newThreeX-newTwoX)*(newOneY-newThreeY);
				if(denom==0) continue;
				// double inverseDenom=1/denom;
				double inverseDenom=1/denom;
				// System.out.println(denom);
				double weight_one_inc_1=(newTwoY-newThreeY)*inverseDenom;
				double weight_two_inc_1=(newThreeY-newOneY)*inverseDenom;
				double weight_three_inc_1=(newOneY-newTwoY)*inverseDenom;
				// if(weight_one_inc_1>1)
				// 	System.out.println(weight_one_inc_1);
				double inverseWeightOneInc=1/weight_one_inc_1;
				double inverseWeightTwoInc=1/weight_two_inc_1;
				double inverseWeightThreeInc=1/weight_three_inc_1;
				double weight_one_inc_2=(newThreeX-newTwoX)*inverseDenom;
				double weight_two_inc_2=(newOneX-newThreeX)*inverseDenom;
				double weight_three_inc_2=(newTwoX-newOneX)*inverseDenom;
				double disToZeroInc1=-weight_one_inc_2*inverseWeightOneInc;
				double disToZeroInc2=-weight_two_inc_2*inverseWeightTwoInc;
				double disToZeroInc3=-weight_three_inc_2*inverseWeightThreeInc;
				// double interpolated_inc=weight_one_inc_1*oneZ+weight_two_inc_1*twoZ+weight_three_inc_1*threeZ;
				// double interpolated_inc_2=weight_one_inc_2*oneZ+weight_two_inc_2*twoZ+weight_three_inc_2*threeZ;
				// illum_inc=illum_1*weight_one_inc_1+illum_2*weight_two_inc_1+illum_3*weight_three_inc_1;
				// pixels=arr;
				// width=a7;
				// height=a8;
				// correct_num_inc_1=weight_one_inc_1*pic_x1/z1+weight_two_inc_1*pic_x2/z2+weight_three_inc_1*pic_x3/z3;
				// correct_num_inc_2=weight_one_inc_1*pic_y1/z1+weight_two_inc_1*pic_y2/z2+weight_three_inc_1*pic_y3/z3;
				// denom_inc=weight_one_inc_1/z1+weight_two_inc_1/z2+weight_three_inc_1/z3;
				// correct_denom_inc=weight_one_inc_2/z1+weight_two_inc_2/z2+weight_three_inc_2/z3;
				// correct_num_1_inc=weight_one_inc_2*pic_x1/z1+weight_two_inc_2*pic_x2/z2+weight_three_inc_2*pic_x3/z3;
				// correct_num_2_inc=weight_one_inc_2*pic_y1/z1+weight_two_inc_2*pic_y2/z2+weight_three_inc_2*pic_y3/z3;
				double lightIntensityInc1=weight_one_inc_1*lightIntensityOne+weight_two_inc_1*lightIntensityTwo+weight_three_inc_1*lightIntensityThree;
				double lightIntensityInc2=weight_one_inc_2*lightIntensityOne+weight_two_inc_2*lightIntensityTwo+weight_three_inc_2*lightIntensityThree;
				double weight_one=((newTwoY-newThreeY)*(-newThreeX)+(newThreeX-newTwoX)*((int)minY-newThreeY))*inverseDenom;
				double weight_two=((newThreeY-newOneY)*(-newThreeX)+(newOneX-newThreeX)*((int)minY-newThreeY))*inverseDenom;
				double weight_three=1-weight_one-weight_two;
				double disToZero1=-weight_one*inverseWeightOneInc;
				double disToZero2=-weight_two*inverseWeightTwoInc;
				double disToZero3=-weight_three*inverseWeightThreeInc;
				double lightIntensityCur=weight_one*lightIntensityOne+weight_two*lightIntensityTwo+weight_three*lightIntensityThree;
				// double interpolated_z_cur=oneZ*weight_one+twoZ*weight_two+threeZ*weight_three;
				int count=0;
				for (int y = (int)minY; y < maxY; y++) {
					double inc_1=weight_one_inc_1;
					double inc_2=weight_two_inc_1;
					double inc_3=weight_three_inc_1;

					double one=Math.ceil(disToZero1);
					double two=Math.ceil(disToZero2);
					double three=Math.ceil(disToZero3);

					if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0))
					{
						lightIntensityCur+=lightIntensityInc2;
						// interpolated_z_cur+=interpolated_inc_2;
						// correct_denom_cur+=correct_denom_inc;
						// correct_num_1_cur+=correct_num_1_inc;
						// correct_num_2_cur+=correct_num_2_inc;
						disToZero1+=disToZeroInc1;
						disToZero2+=disToZeroInc2;
						disToZero3+=disToZeroInc3;
						continue;
					}
					// double interpolated_z=interpolated_z_cur;

					// double correct_denom=correct_denom_cur;
					// double denom_inc=denom_inc;

					// double correct_num_1=correct_num_1_cur;
					// double correct_num_2=correct_num_2_cur;

					// double correct_num_inc_1=correct_num_inc_1;
					// double correct_num_inc_2=correct_num_inc_2;

					double lightIntensity=lightIntensityCur;

					// int[] color=pixels;
					// int height=height;
					// int width=width;
					int min=0;
					if(inc_1>0&&one>0)
						min=Math.max(min,(int)one);
					if(inc_2>0&&two>0)
						min=Math.max(min,(int)two);
					if(inc_3>0&&three>0)
						min=Math.max(min,(int)three);
					lightIntensity+=lightIntensityInc1*min;
					// weight_one+=inc_1*min;
					// weight_two+=inc_2*min;
					// weight_three+=inc_3*min;
					// interpolated_z+=interpolated_inc*min;
					// correct_denom+=denom_inc*min;
					// correct_num_1+=correct_num_inc_1*min;
					// correct_num_2+=correct_num_inc_2*min;
					// double disToZero1=-weight_one/inc_1;
					// double disToZero2=-weight_two/inc_2;
					// double disToZero3=-weight_three/inc_3;
					int max=640;
					// if((one<0&&inc_1<0)||(two<0&&inc_2<0)||(three<0&&inc_3<0)) max=0;
					if(inc_1<0)
						max=Math.min(max,(int)one);
					if(inc_2<0)
						max=Math.min(max,(int)two);
					if(inc_3<0)
						max=Math.min(max,(int)three);
					// pixelsDrawn.updateY(0,0,639,y,min,max);
					for(int x=min;x<max;x++)
					{
						// if(x!=min&&x!=Math.floor(max)-1) continue;
						numLoops++;
						if(!zbufferUsed[y*640+x])
						{
							flag1=false;
							num++;
							// if(!zbufferUsed[y*640+x])
							// 	unique++;
							zbufferUsed[y*640+x]=true;
							// zbuffer[y*640+x]=interpolated_z;
							// pixels[y*640+x]=0;
							if(key_listener.change) continue;
							if(lightIntensity<0)
								pixels[y*640+x]=0;
							else if(lightIntensity>255)
								pixels[y*640+x]=0b111111111111111111111111;
							else
							{
								int red=(int)lightIntensity<<16;
								int green=(int)lightIntensity<<8;
								int blue=(int)lightIntensity;
								pixels[y*640+x]=red+green+blue;
							}
							// if(color==null)
							// {
								// double lightIntensity=weight_one*curTriangle.lightIntensityOne+weight_two*curTriangle.lightIntensityTwo+weight_three*curTriangle.lightIntensityThree;
							// }
							// else
							// {
							// 	int texture_x=(int)(correct_num_1/correct_denom);
							// 	int texture_y=(int)(correct_num_2/correct_denom);
							// 	if(texture_y>=height||texture_x>=width||texture_x<0||texture_y<0) continue;
							// 	int red=color[texture_y*width+texture_x]>>16&255;
							// 	int green=color[texture_y*width+texture_x]>>8&255;
							// 	int blue=color[texture_y*width+texture_x]&255;
							// 	// pixels[y*640+x]=new Color((int)Math.max(0,Math.min(255,red/illum)),(int)Math.max(0,Math.min(255,green/illum)),(int)Math.max(0,Math.min(255,blue/illum))).getRGB();
							// }
						}
						lightIntensity+=lightIntensityInc1;
						// interpolated_z+=interpolated_inc;
						// correct_denom+=denom_inc;
						// correct_num_1+=correct_num_inc_1;
						// correct_num_2+=correct_num_inc_2;
					}
					lightIntensityCur+=lightIntensityInc2;
					// interpolated_z_cur+=interpolated_inc_2;
					// correct_denom_cur+=correct_denom_inc;
					// correct_num_1_cur+=correct_num_1_inc;
					// correct_num_2_cur+=correct_num_2_inc;
					disToZero1+=disToZeroInc1;
					disToZero2+=disToZeroInc2;
					disToZero3+=disToZeroInc3;
				}
				twoX=threeX;
				twoY=threeY;
				twoZ=threeZ;
				surfaceNormalTwoX=surfaceNormalThreeX;
				surfaceNormalTwoY=surfaceNormalThreeY;
				surfaceNormalTwoZ=surfaceNormalThreeZ;
				lightIntensityTwo=lightIntensityThree;
				newTwoX=newThreeX;
				newTwoY=newThreeY;
			}
			// if(!flag1)
			// 	newOccluders.add(curPolygon.ID);
			noDraw+=flag1?1:0;
		}
		totOccluded+=numOccluded;
		totNoDraw+=noDraw;
		if(cur.classifyPoint(cur.current[0],cameraPos)==1)
			render(cur.behind,pixels);
		else
			render(cur.inFront,pixels);
	}
	public boolean recurse(int depth,int prevXPos,int prevYPos,int minXBound,int maxXBound,int minYBound,int maxYBound)//true==completely occluded, false=not
	{
		if(depth==8) return false;
		int LBound=(int)(minXBound/(1<<7-depth));
		int RBound=(int)(maxXBound/(1<<7-depth));
		int UBound=(int)(minYBound/(1<<7-depth));
		int DBound=(int)(maxYBound/(1<<7-depth));
		if(depth==0)
		{
			if(HOMMaps[depth][prevYPos][prevXPos]<1-1E-9)
				return recurse(depth+1,prevXPos,prevYPos,minXBound,maxXBound,minYBound,maxYBound);
			return true;
		}
		else
		{
			boolean flag1;
			if(prevXPos*2>=minXBound&&prevXPos*2<=maxXBound&&prevYPos*2>=minYBound&&prevYPos*2<=maxYBound)
				flag1=HOMMaps[depth][prevYPos*2][prevXPos*2]<1-1E-9?recurse(depth+1,prevXPos*2,prevYPos*2,minXBound,maxXBound,minYBound,maxYBound):true;
			else
				flag1=true;
			boolean flag2;
			if(prevXPos*2+1>=minXBound&&prevXPos*2+1<=maxXBound&&prevYPos*2>=minYBound&&prevYPos*2<=maxYBound)
				flag2=HOMMaps[depth][prevYPos*2][prevXPos*2+1]<1-1E-9?recurse(depth+1,prevXPos*2+1,prevYPos*2,minXBound,maxXBound,minYBound,maxYBound):true;
			else
				flag2=true;
			boolean flag3;
			if(prevXPos*2>=minXBound&&prevXPos*2<=maxXBound&&prevYPos*2+1>=minYBound&&prevYPos*2+1<=maxYBound)
				flag3=HOMMaps[depth][prevYPos*2+1][prevXPos*2]<1-1E-9?recurse(depth+1,prevXPos*2,prevYPos*2+1,minXBound,maxXBound,minYBound,maxYBound):true;
			else
				flag3=true;
			boolean flag4;
			if(prevXPos*2+1>=minXBound&&prevXPos*2+1<=maxXBound&&prevYPos*2+1>=minYBound&&prevYPos*2+1<=maxYBound)
				flag4=HOMMaps[depth][prevYPos*2+1][prevXPos*2+1]<1-1E-9?recurse(depth+1,prevXPos*2+1,prevYPos*2+1,minXBound,maxXBound,minYBound,maxYBound):true;
			else
				flag4=true;
			return flag1&&flag2&&flag3&&flag4;
		}
	}
	public polygon split(polygon a,double[] divider)
	{
		int val=calculateSide(divider,a);
		if(val==SPANNING)
		{
			Vec3d[] posPoints=new Vec3d[100];
			Vec3d[] negPoints=new Vec3d[100];
			int numPosPoints=0;
			int numNegPoints=0;
			Vec3d prev=a.points[a.points.length-1];
			int sideA=classifyPoint(divider,prev);
			for (int j = 0; j < a.points.length; j++) {
				int sideB=classifyPoint(divider,a.points[j]);
				if(sideB==INFRONT)
				{
					if(sideA==BEHIND)
					{
						double lineX=a.points[j].one-prev.one;
						double lineY=a.points[j].two-prev.two;
						double lineZ=a.points[j].three-prev.three;
						double dotProdOne=divider[0]*(0-prev.one)+divider[1]*(0-prev.two)+divider[2]*(0-prev.three);
						double dotProdTwo=divider[0]*lineX+divider[1]*lineY+divider[2]*lineZ;
						double newPosX=prev.one+(lineX*(dotProdOne/dotProdTwo));
						double newPosY=prev.two+(lineY*(dotProdOne/dotProdTwo));
						double newPosZ=prev.three+(lineZ*(dotProdOne/dotProdTwo));
						posPoints[numPosPoints]=negPoints[numNegPoints]=new Vec3d(newPosX,newPosY,newPosZ);
						numPosPoints++;
						numNegPoints++;
					}
					posPoints[numPosPoints]=a.points[j];
					numPosPoints++;
				}
				else if(sideB==BEHIND)
				{
					if(sideA==INFRONT)
					{
						double lineX=a.points[j].one-prev.one;
						double lineY=a.points[j].two-prev.two;
						double lineZ=a.points[j].three-prev.three;
						double dotProdOne=divider[0]*(0-prev.one)+divider[1]*(0-prev.two)+divider[2]*(0-prev.three);
						double dotProdTwo=divider[0]*lineX+divider[1]*lineY+divider[2]*lineZ;
						double newPosX=prev.one+(lineX*(dotProdOne/dotProdTwo));
						double newPosY=prev.two+(lineY*(dotProdOne/dotProdTwo));
						double newPosZ=prev.three+(lineZ*(dotProdOne/dotProdTwo));
						posPoints[numPosPoints]=negPoints[numNegPoints]=new Vec3d(newPosX,newPosY,newPosZ);
						numPosPoints++;
						numNegPoints++;
					}
					negPoints[numNegPoints]=a.points[j];
					numNegPoints++;
				}
				else
				{
					posPoints[numPosPoints]=a.points[j];
					negPoints[numNegPoints]=a.points[j];
					numPosPoints++;
					numNegPoints++;
				}
				prev=a.points[j];
				sideA=sideB;
			}
			Vec3d[] tempFront=new Vec3d[numPosPoints];
			for (int j = 0; j < numPosPoints; j++) {
				tempFront[j]=posPoints[j];
			}
			Vec3d[] tempBehind=new Vec3d[numNegPoints];
			for (int j = 0; j < numNegPoints; j++) {
				tempBehind[j]=negPoints[j];
			}
			// polygon front=new polygon(tempFront,a.origPointOne,a.origPointTwo,a.origPointThree,a.surfaceNormalOne,a.surfaceNormalTwo,a.surfaceNormalThree,a.polygonSurfaceNorm,a.dis);
			polygon behind=new polygon(tempBehind,a.origPointOne,a.origPointTwo,a.origPointThree,a.surfaceNormalOne,a.surfaceNormalTwo,a.surfaceNormalThree,a.polygonSurfaceNorm,a.dis);
			// positive[numPos]=front;
			return behind;
			// numPos++;
			// numNeg++;
		}
		else if(val==INFRONT)
			return null;
		else
			return a;
		// return null;
	}
	public int calculateSide(double[] normal,polygon b)
	{
		int numPos=0;
		int numNeg=0;
		for (int i = 0; i < b.points.length; i++) {
			if(classifyPoint(normal,b.points[i])==INFRONT)
				numPos++;
			else if(classifyPoint(normal,b.points[i])==BEHIND)
				numNeg++;
		}
		if(numPos>0&&numNeg==0)
			return INFRONT;//in front
		else if(numPos==0&&numNeg>0)
			return BEHIND;//behind
		else if(numPos==0&&numNeg==0)
			return COINCIDING;//coincide
		else
			return SPANNING;//spanning
	}
	public int classifyPoint(double[] a,Vec3d b)
	{
		double dis=a[0]*b.one+a[1]*b.two+a[2]*b.three;
		if(Math.abs(dis)<1E-9)
			return COINCIDING;//coinciding
		if(dis<0)
			return BEHIND;//behind
		return INFRONT;//front
	}
	public boolean inFront(double[] a,polygon b)
	{
		for (int i = 0; i < b.points.length; i++) {
			if(classifyPoint(a,b.points[i])!=INFRONT)
				return false;
		}
		return true;
	}
}
class temp implements Comparable<temp>
{
	double disToZero,inc;
	public temp(double a,double b)
	{
		disToZero=a;
		inc=b;
	}
	public int compareTo(temp a)
	{
		return disToZero<a.disToZero?-1:1;
	}
}