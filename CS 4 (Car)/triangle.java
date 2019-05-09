public class triangle
{
	double origOneX,origOneY,origOneZ;
	double origTwoX,origTwoY,origTwoZ;
	double origThreeX,origThreeY,origThreeZ;
	double curOneX,curOneY,curOneZ;
	double curTwoX,curTwoY,curTwoZ;
	double curThreeX,curThreeY,curThreeZ;
	surfaceNormal surfaceNormalOne,surfaceNormalTwo,surfaceNormalThree;
	public triangle(Vec3d one,Vec3d two,Vec3d three,Vec3d newOne,Vec3d newTwo,Vec3d newThree,surfaceNormal normOne,surfaceNormal normTwo,surfaceNormal normThree)
	{
		origOneX=one.one;
		origOneY=one.two;
		origOneZ=one.three;
		origTwoX=two.one;
		origTwoY=two.two;
		origTwoZ=two.three;
		origThreeX=three.one;
		origThreeY=three.two;
		origThreeZ=three.three;

		curOneX=newOne.one;
		curOneY=newOne.two;
		curOneZ=newOne.three;
		curTwoX=newTwo.one;
		curTwoY=newTwo.two;
		curTwoZ=newTwo.three;
		curThreeX=newThree.one;
		curThreeY=newThree.two;
		curThreeZ=newThree.three;

		surfaceNormalOne=normOne;
		surfaceNormalTwo=normTwo;
		surfaceNormalThree=normThree;
	}
	public String toString()
	{
		// return "x1: "+curOneX+" y1: "+curOneY+" z1: "+curOneZ+" x2: "+curTwoX+" y2: "+curTwoY+" z2: "+curTwoZ+" x3: "+curThreeX+" y3: "+curThreeY+" z3: "+curThreeZ;
		return "xRatio1: "+curOneX/curOneZ+" yRatio1: "+curOneY/curOneZ+" xRatio2: "+curTwoX/curTwoZ+" yRatio2: "+curTwoY/curTwoZ+" xRatio3: "+curThreeX/curThreeZ+" yRatio3: "+curThreeY/curThreeZ;
	}
}