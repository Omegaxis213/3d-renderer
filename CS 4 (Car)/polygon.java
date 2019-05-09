import java.util.*;
class polygon
{
	// ArrayList<Vec3d> origPoints;//make sure this always stays in either clockwise or counterclockwise order
	// ArrayList<Vec3d> points;//make sure this always stays in either clockwise or counterclockwise order
	Vec3d origPointOne;
	Vec3d origPointTwo;
	Vec3d origPointThree;
	Vec3d[] points;
	surfaceNormal surfaceNormalOne,surfaceNormalTwo,surfaceNormalThree;
	surfaceNormal polygonSurfaceNorm;
	double dis;
	Vec3d[] boundingBox;
	Vec3d centerPoint;
	int ID;
	public polygon(Vec3d[] a,Vec3d one,Vec3d two,Vec3d three,surfaceNormal normOne,surfaceNormal normTwo,surfaceNormal normThree,surfaceNormal polygon,double Dis)
	{
		points=a;
		// origPoints=new ArrayList<Vec3d>();
		origPointOne=one;
		origPointTwo=two;
		origPointThree=three;
		surfaceNormalOne=normOne;
		surfaceNormalTwo=normTwo;
		surfaceNormalThree=normThree;
		polygonSurfaceNorm=polygon;
		dis=Dis;
	}
	public void createBoundingBox()
	{
		boundingBox=new Vec3d[8];
		double minX=Double.POSITIVE_INFINITY;
		double minY=Double.POSITIVE_INFINITY;
		double minZ=Double.POSITIVE_INFINITY;
		double maxX=Double.NEGATIVE_INFINITY;
		double maxY=Double.NEGATIVE_INFINITY;
		double maxZ=Double.NEGATIVE_INFINITY;
		for (int i = 0; i < points.length; i++) {
			minX=Math.min(minX,points[i].one);
			maxX=Math.max(maxX,points[i].one);
			minY=Math.min(minY,points[i].two);
			maxY=Math.max(maxY,points[i].two);
			minZ=Math.min(minZ,points[i].three);
			maxZ=Math.max(maxZ,points[i].three);
		}
		boundingBox[0]=new Vec3d(minX,minY,minZ);
		boundingBox[1]=new Vec3d(minX,minY,maxZ);
		boundingBox[2]=new Vec3d(minX,maxY,minZ);
		boundingBox[3]=new Vec3d(minX,maxY,maxZ);
		boundingBox[4]=new Vec3d(maxX,minY,minZ);
		boundingBox[5]=new Vec3d(maxX,minY,maxZ);
		boundingBox[6]=new Vec3d(maxX,maxY,minZ);
		boundingBox[7]=new Vec3d(maxX,maxY,maxZ);
		// centerPoint=new Vec3d((minX+maxX)/2,(minY+maxY)/2,(minZ+maxZ)/2);
	}
	public String toString()
	{
		return Arrays.deepToString(points);
	}
	// public void update(ArrayList<Vec3d> a)
	// {
	// 	for (int i = 0; i < a.size(); i++) {
	// 		origPoints.add(a.get(i));
	// 	}
	// }
}