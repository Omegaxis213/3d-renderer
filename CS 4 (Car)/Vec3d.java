class Vec3d
{
	double one;
	double two;
	double three;
	public Vec3d(double a,double b,double c)
	{
		one=a;
		two=b;
		three=c;
	}
	public String toString()
	{
		return "xratio: "+one/three+" yratio: "+two/three+" "+three;
	}
}