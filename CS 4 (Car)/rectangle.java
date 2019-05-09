class rectangle
{
	int lowX,highX,lowY,highY;
	public rectangle(int a,int b,int c,int d)
	{
		lowX=a;
		highX=b;
		lowY=c;
		highY=d;
	}
	public String toString()
	{
		return "lowX: "+lowX+" highX: "+highX+" lowY: "+lowY+" highY: "+highY;
	}
}