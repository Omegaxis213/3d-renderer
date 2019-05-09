import java.util.*;
class binarySpacePartitionTree
{
	binarySpacePartitionTree inFront,behind;
	polygon[] current;
	ArrayList<ArrayList<triangle>> drawTriangle;
	int numCur;
	final int INFRONT=1;
	final int BEHIND=-1;
	final int SPANNING=2;
	final int COINCIDING=0;
	public binarySpacePartitionTree(polygon[] list,int size)
	{
		if(list==null||size==0) return;
		// System.out.println(Arrays.toString(list));
		drawTriangle=new ArrayList<ArrayList<triangle>>();
		if(isConvexSet(list,size))
		{
			current=list;
			return;
		}
		current=new polygon[size];
		// System.out.println(size);
		polygon divider=bestPolygon(list,size);
		// polygon divider=list[size/2];
		if(divider==null)
		{
			current=list;
			return;
		}
		// System.out.println(size);
		polygon[] positive=new polygon[size];
		polygon[] negative=new polygon[size];
		int numPos=0;
		int numNeg=0;
		int max=0;
		for (int i = 0; i < size; i++) {
			int val=calculateSide(divider,list[i]);
			if(val==INFRONT)
			{
				positive[numPos]=list[i];
				numPos++;
			}
			else if(val==BEHIND)
			{
				negative[numNeg]=list[i];
				numNeg++;
			}
			else if(val==SPANNING)
			{
				Vec3d[] posPoints=new Vec3d[100];
				Vec3d[] negPoints=new Vec3d[100];
				int numPosPoints=0;
				int numNegPoints=0;
				Vec3d prev=list[i].points[list[i].points.length-1];
				int sideA=classifyPoint(divider,prev);
				for (int j = 0; j < list[i].points.length; j++) {
					int sideB=classifyPoint(divider,list[i].points[j]);
					if(sideB==INFRONT)
					{
						if(sideA==BEHIND)
						{
							double lineX=list[i].points[j].one-prev.one;
							double lineY=list[i].points[j].two-prev.two;
							double lineZ=list[i].points[j].three-prev.three;
							double dotProdOne=divider.polygonSurfaceNorm.avgNormX*(divider.points[0].one-prev.one)+divider.polygonSurfaceNorm.avgNormY*(divider.points[0].two-prev.two)+divider.polygonSurfaceNorm.avgNormZ*(divider.points[0].three-prev.three);
							double dotProdTwo=divider.polygonSurfaceNorm.avgNormX*lineX+divider.polygonSurfaceNorm.avgNormY*lineY+divider.polygonSurfaceNorm.avgNormZ*lineZ;
							double newPosX=prev.one+(lineX*(dotProdOne/dotProdTwo));
							double newPosY=prev.two+(lineY*(dotProdOne/dotProdTwo));
							double newPosZ=prev.three+(lineZ*(dotProdOne/dotProdTwo));
							posPoints[numPosPoints]=negPoints[numNegPoints]=new Vec3d(newPosX,newPosY,newPosZ);
							numPosPoints++;
							numNegPoints++;
						}
						posPoints[numPosPoints]=list[i].points[j];
						numPosPoints++;
					}
					else if(sideB==BEHIND)
					{
						if(sideA==INFRONT)
						{
							double lineX=list[i].points[j].one-prev.one;
							double lineY=list[i].points[j].two-prev.two;
							double lineZ=list[i].points[j].three-prev.three;
							double dotProdOne=divider.polygonSurfaceNorm.avgNormX*(divider.points[0].one-prev.one)+divider.polygonSurfaceNorm.avgNormY*(divider.points[0].two-prev.two)+divider.polygonSurfaceNorm.avgNormZ*(divider.points[0].three-prev.three);
							double dotProdTwo=divider.polygonSurfaceNorm.avgNormX*lineX+divider.polygonSurfaceNorm.avgNormY*lineY+divider.polygonSurfaceNorm.avgNormZ*lineZ;
							double newPosX=prev.one+(lineX*(dotProdOne/dotProdTwo));
							double newPosY=prev.two+(lineY*(dotProdOne/dotProdTwo));
							double newPosZ=prev.three+(lineZ*(dotProdOne/dotProdTwo));
							posPoints[numPosPoints]=negPoints[numNegPoints]=new Vec3d(newPosX,newPosY,newPosZ);
							numPosPoints++;
							numNegPoints++;
						}
						negPoints[numNegPoints]=list[i].points[j];
						numNegPoints++;
					}
					else
					{
						posPoints[numPosPoints]=list[i].points[j];
						negPoints[numNegPoints]=list[i].points[j];
						numPosPoints++;
						numNegPoints++;
					}
					prev=list[i].points[j];
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
				polygon front=new polygon(tempFront,list[i].origPointOne,list[i].origPointTwo,list[i].origPointThree,list[i].surfaceNormalOne,list[i].surfaceNormalTwo,list[i].surfaceNormalThree,list[i].polygonSurfaceNorm,list[i].dis);
				polygon behind=new polygon(tempBehind,list[i].origPointOne,list[i].origPointTwo,list[i].origPointThree,list[i].surfaceNormalOne,list[i].surfaceNormalTwo,list[i].surfaceNormalThree,list[i].polygonSurfaceNorm,list[i].dis);
				positive[numPos]=front;
				negative[numNeg]=behind;
				numPos++;
				numNeg++;
			}
			else
			{
				current[numCur]=list[i];
				numCur++;
			}
		}
		// for (int i=0;i<list.size();i++) {
		// 	polygon curPolygon=list.get(i);
		// 	double[] vals=new double[curPolygon.points.size()];
		// 	for (int j = 0; j < vals.length; j++) {
		// 		vals[j]=curPolygon.points.get(j).one*normalX+curPolygon.points.get(j).two*normalY+curPolygon.points.get(j).three*normalZ-constVal;
		// 	}
		// 	max=Math.max(max,curPolygon.points.size());
		// 	// System.out.println(i+" "+Arrays.toString(vals));
		// 	boolean allPos=true;
		// 	boolean allNeg=true;
		// 	boolean allZero=true;
		// 	for (int j = 0; j < vals.length; j++) {
		// 		if(vals[j]<0)
		// 			allPos=false;
		// 		if(vals[j]>0)
		// 			allNeg=false;
		// 		if(vals[j]!=0)
		// 			allZero=false;
		// 	}
		// 	if(allPos)
		// 	{
		// 		listInFront.add(curPolygon);
		// 		continue;
		// 	}
		// 	else if(allNeg)
		// 	{
		// 		listBehind.add(curPolygon);
		// 		continue;
		// 	}
		// 	else if(allZero)
		// 	{
		// 		current.add(curPolygon);
		// 		continue;
		// 	}
		// 	int startPos=-1;
		// 	int endPos=-1;
		// 	for (int j = 0; j < curPolygon.points.size(); j++) {
		// 		int signOne=vals[j]<0?-1:1;
		// 		int signTwo=vals[(j+1)%vals.length]<0?-1:1;
		// 		if(signOne!=signTwo)
		// 		{
		// 			if(startPos==-1)
		// 				startPos=j+1;
		// 			else if(endPos==-1)
		// 				endPos=j;
		// 		}
		// 	}
		// 	if(endPos==-1)
		// 		endPos=startPos;
		// 	double dotProductOneX=(tempPolygon.points.get(0).one-curPolygon.points.get(startPos).one)*normalX;
		// 	double dotProductOneY=(tempPolygon.points.get(0).two-curPolygon.points.get(startPos).two)*normalY;
		// 	double dotProductOneZ=(tempPolygon.points.get(0).three-curPolygon.points.get(startPos).three)*normalZ;
		// 	double dotProductOne=dotProductOneX+dotProductOneY+dotProductOneZ;
		// 	double lineVectorOneX=curPolygon.points.get(startPos).one-curPolygon.points.get(startPos-1).one;
		// 	double lineVectorOneY=curPolygon.points.get(startPos).two-curPolygon.points.get(startPos-1).two;
		// 	double lineVectorOneZ=curPolygon.points.get(startPos).three-curPolygon.points.get(startPos-1).three;
		// 	double lineDotProductOne=lineVectorOneX*normalX+lineVectorOneY*normalY+lineVectorOneZ*normalZ;
		// 	double disOne=dotProductOne/lineDotProductOne;
		// 	double pointOneX=curPolygon.points.get(startPos-1).one+lineVectorOneX*disOne;
		// 	double pointOneY=curPolygon.points.get(startPos-1).two+lineVectorOneY*disOne;
		// 	double pointOneZ=curPolygon.points.get(startPos-1).three+lineVectorOneZ*disOne;

		// 	double dotProductTwoX=(tempPolygon.points.get(0).one-curPolygon.points.get(endPos).one)*normalX;
		// 	double dotProductTwoY=(tempPolygon.points.get(0).two-curPolygon.points.get(endPos).two)*normalY;
		// 	double dotProductTwoZ=(tempPolygon.points.get(0).three-curPolygon.points.get(endPos).three)*normalZ;
		// 	double dotProductTwo=dotProductOneX+dotProductOneY+dotProductOneZ;
		// 	double lineVectorTwoX=curPolygon.points.get(endPos).one-curPolygon.points.get((endPos+1)%vals.length).one;
		// 	double lineVectorTwoY=curPolygon.points.get(endPos).two-curPolygon.points.get((endPos+1)%vals.length).two;
		// 	double lineVectorTwoZ=curPolygon.points.get(endPos).three-curPolygon.points.get((endPos+1)%vals.length).three;
		// 	double lineDotProductTwo=lineVectorTwoX*normalX+lineVectorTwoY*normalY+lineVectorTwoZ*normalZ;
		// 	double disTwo=dotProductTwo/lineDotProductTwo;
		// 	double pointTwoX=curPolygon.points.get((endPos+1)%vals.length).one+lineVectorTwoX*disTwo;
		// 	double pointTwoY=curPolygon.points.get((endPos+1)%vals.length).two+lineVectorTwoY*disTwo;
		// 	double pointTwoZ=curPolygon.points.get((endPos+1)%vals.length).three+lineVectorTwoZ*disTwo;

		// 	ArrayList<Vec3d> points1=new ArrayList<Vec3d>();
		// 	points1.add(new Vec3d(pointOneX,pointOneY,pointOneZ));
		// 	for (int j = startPos; j <= endPos; j++) {
		// 		points1.add(curPolygon.points.get(j));
		// 	}
		// 	points1.add(new Vec3d(pointTwoX,pointTwoY,pointTwoZ));

		// 	ArrayList<Vec3d> points2=new ArrayList<Vec3d>();
		// 	points2.add(new Vec3d(pointTwoX,pointTwoY,pointTwoZ));
		// 	int curPos=(endPos+1)%vals.length;
		// 	while(curPos!=startPos)
		// 	{
		// 		points2.add(curPolygon.points.get(curPos));
		// 		curPos=(curPos+1)%vals.length;
		// 	}
		// 	points2.add(new Vec3d(pointOneX,pointOneY,pointOneZ));

		// 	if(vals[startPos]>0)
		// 	{
		// 		polygon temp=new polygon(points1,curPolygon.surfaceNormalOne,curPolygon.surfaceNormalTwo,curPolygon.surfaceNormalThree,curPolygon.polygonSurfaceNorm,curPolygon.dis);
		// 		temp.update(curPolygon.origPoints);
		// 		listInFront.add(temp);
		// 		temp=new polygon(points2,curPolygon.surfaceNormalOne,curPolygon.surfaceNormalTwo,curPolygon.surfaceNormalThree,curPolygon.polygonSurfaceNorm,curPolygon.dis);
		// 		temp.update(curPolygon.origPoints);
		// 		listBehind.add(temp);
		// 	}
		// 	else if(vals[endPos]<0)
		// 	{
		// 		polygon temp=new polygon(points2,curPolygon.surfaceNormalOne,curPolygon.surfaceNormalTwo,curPolygon.surfaceNormalThree,curPolygon.polygonSurfaceNorm,curPolygon.dis);
		// 		temp.update(curPolygon.origPoints);
		// 		listInFront.add(temp);
		// 		temp=new polygon(points1,curPolygon.surfaceNormalOne,curPolygon.surfaceNormalTwo,curPolygon.surfaceNormalThree,curPolygon.polygonSurfaceNorm,curPolygon.dis);
		// 		temp.update(curPolygon.origPoints);
		// 		listBehind.add(temp);
		// 	}
		// 	else
		// 	{
		// 	}
		// }
		// if(list.size()>10000)
			// System.out.println(list.size()+" "+listInFront.size()+" "+listBehind.size());
		// System.out.println(list.length+" "+max);
		// System.out.println(size+" "+numPos+" "+numNeg+" "+numCur);
		inFront=new binarySpacePartitionTree(positive,numPos);
		behind=new binarySpacePartitionTree(negative,numNeg);
		screen.processed+=numPos+numNeg;
		// System.out.println(screen.processed);
	}
	public polygon bestPolygon(polygon[] a,int size)
	{
		if(isConvexSet(a,size))
			return null;
		polygon best=null;
		double minRelation=.8;
		double minRelationScale=2;
		double leastSplits=Double.POSITIVE_INFINITY;
		double bestRelation=0;
		polygon bestZeroPolygon=null;
		double smallest=1<<20;
		boolean flag=true;
		int count=0;
		while(best==null)
		{
			int number=50;
			for(int i=0;i<number;i++)
			{
				int pos1=(int)(Math.random()*size);
				// polygon temp=a.get(i);
				double numInFront=0;
				double numBehind=0;
				double numSpanning=0;
				int max=0;
				for(int k=0;k<size;k++)
				{
					if(pos1==k) continue;
					// polygon temp1=a.get(k);
					int val=calculateSide(a[pos1],a[k]);
					if(val==INFRONT)
						numInFront++;
					else if(val==BEHIND)
						numBehind++;
					else if(val==SPANNING)
						numSpanning++;
				}
				// if(count%1000==0)
				// 	System.out.println(count+" "+max+" "+a.length+" "+best);
				// count++;
				// if(numInFront!=0||numBehind!=0)
				// 	System.out.println(numInFront+" "+numBehind);
				double relation=0;
				if(numInFront<numBehind)
				{
					if(numInFront==0)
					{
						if(numBehind<smallest)
						{
							smallest=numBehind;
							bestZeroPolygon=a[pos1];
						}
					}
					else flag=false;
					relation=numInFront/numBehind;
				}
				else
				{
					if(numBehind==0)
					{
						if(numInFront<smallest)
						{
							smallest=numInFront;
							bestZeroPolygon=a[pos1];
						}
					}
					else flag=false;
					relation=numBehind/numInFront;
				}
				// System.out.println(relation+" "+numSpanning+" "+leastSplits+" "+bestRelation+" "+best);
				if(relation>minRelation&&(numSpanning<leastSplits||(numSpanning==leastSplits&&relation>bestRelation)))
				{
					best=a[pos1];
					leastSplits=numSpanning;
					bestRelation=relation;
				}
			}
			if(flag) best=bestZeroPolygon;
			minRelation/=minRelationScale;
		}
		return best;
	}
	public int calculateSide(polygon a,polygon b)
	{
		int numPos=0;
		int numNeg=0;
		for (int i = 0; i < b.points.length; i++) {
			if(classifyPoint(a,b.points[i])==INFRONT)
				numPos++;
			else if(classifyPoint(a,b.points[i])==BEHIND)
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
	public int classifyPoint(polygon a,Vec3d b)
	{
		double dis=a.polygonSurfaceNorm.avgNormX*b.one+a.polygonSurfaceNorm.avgNormY*b.two+a.polygonSurfaceNorm.avgNormZ*b.three;
		if(Math.abs(dis-a.dis)<1E-9)
			return COINCIDING;//coinciding
		if(dis<a.dis)
			return BEHIND;//behind
		return INFRONT;//front
	}
	public boolean inFront(polygon a,polygon b)
	{
		for (int i = 0; i < b.points.length; i++) {
			if(classifyPoint(a,b.points[i])!=INFRONT)
				return false;
		}
		return true;
	}
	public boolean isConvexSet(polygon[] a,int size)
	{
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(i!=j&&!inFront(a[i],a[j]))
					return false;
			}
		}
		return true;
	}
}