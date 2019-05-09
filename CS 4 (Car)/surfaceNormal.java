class surfaceNormal
{
	double avgNormX,avgNormY,avgNormZ;
	int counter;
	boolean isNormalized;
	public surfaceNormal(double d,double e,double f)
	{
		avgNormX=d;
		avgNormY=e;
		avgNormZ=f;
	}
	// public void add(double a,double b,double c)
	// {
	// 	avgNormX+=a;
	// 	avgNormY+=b;
	// 	avgNormZ+=c;
	// 	counter++;
	// }
	// public void update(double a,double b,double c)
	// {
	// 	avgNormX=a;
	// 	avgNormY=b;
	// 	avgNormZ=c;
	// }
	// public void mult(double a)
	// {
	// 	avgNormX*=a;
	// 	avgNormY*=a;
	// 	avgNormZ*=a;
	// 	isNormalized=true;
	// }
}