class point
{
	double x,y,z;
	int hashOne,hashTwo,hashThree;
	double newX,newY;
	public point(double a,double b,double c,int hashOne,int hashTwo,int hashThree,double newX,double newY)
	{
		x=a;
		y=b;
		z=c;
		this.hashOne=hashOne;
		this.hashTwo=hashTwo;
		this.hashThree=hashThree;
		this.newX=newX;
		this.newY=newY;
	}
}