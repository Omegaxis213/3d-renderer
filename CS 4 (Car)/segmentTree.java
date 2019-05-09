import java.util.*;
class segmentTree
{
	boolean[][] arr;
	boolean[][] lazy;
	public segmentTree()
	{
		arr=new boolean[640<<2][640<<2];
		lazy=new boolean[640<<2][640<<2];
	}
	public void updateY(int node,int left,int right,int yPos,int xLeft,int xRight)
	{
		if(left>yPos||right<yPos) return;
		if(lazy[node][0]) return;
		if(left==right&&left==yPos)
		{
			updateX(node,0,0,639,xLeft,xRight);
			return;
		}
		int mid=(left+right)/2;
		updateY(node*2+1,left,mid,yPos,xLeft,xRight);
		updateY(node*2+2,mid+1,right,yPos,xLeft,xRight);
		lazy[node][0]=lazy[node*2+1][0]&lazy[node*2+2][0];
	}
	public void updateX(int yNode,int node,int left,int right,int xLeft,int xRight)
	{
		if(left>xRight||right<xLeft) return;
		if(lazy[yNode][node]) return;
		if(left>=xLeft&&right<=xRight)
		{
			lazy[yNode][node]=true;
			return;
		}
		if(lazy[yNode][node])
		{
			arr[yNode][node]=true;
			lazy[yNode][node*2+1]=lazy[yNode][node*2+2]=true;
			lazy[yNode][node]=false;
		}
		int mid=(left+right)/2;
		updateX(yNode,node*2+1,left,mid,xLeft,xRight);
		updateX(yNode,node*2+2,mid+1,right,xLeft,xRight);
		arr[yNode][node]=arr[yNode][node*2+1]&arr[yNode][node*2+2];
	}
	public boolean queryY(int node,int left,int right,int yLeft,int yRight,int xLeft,int xRight)
	{
		if(left>yRight||right<yLeft) return true;
		if(left>=yLeft&&right<=yRight)
		{
			return queryX(node,0,0,639,xLeft,xRight);
		}
		// if(lazy[node][0])
		// {
		// 	arr[node][0]=true;
		// 	lazy[node*2+1][0]=lazy[node*2+2][0]=true;
		// 	lazy[node][0]=false;
		// }
		int mid=(left+right)/2;
		return queryY(node*2+1,left,mid,yLeft,yRight,xLeft,xRight)&queryY(node*2+2,mid+1,right,yLeft,yRight,xLeft,xRight);
	}
	public boolean queryX(int yNode,int node,int left,int right,int xLeft,int xRight)
	{
		if(left>xRight||right<xLeft) return true;
		if(left>=xLeft&&right<=xRight)
			return lazy[yNode][node];
		if(lazy[yNode][node])
		{
			arr[yNode][node]=true;
			lazy[yNode][node*2+1]=lazy[yNode][node*2+2]=true;
			lazy[yNode][node]=false;
		}
		int mid=(left+right)/2;
		return queryX(yNode,node*2+1,left,mid,xLeft,xRight)&queryX(yNode,node*2+2,mid+1,right,xLeft,xRight);
	}
}