import java.util.concurrent.locks.*;

public class FineLockTree {
	
	class Node{
		int element;
		Node left;
		Node right;	
		
		public Node(int x){
			element = x;
			left = null;
			right = null;
		}
	}
	
	private Node root;
	private ReentrantLock topLock;
	
	public FineLockTree(){
		root = null;
	}
	
	public boolean contains(int x){
		Node current = root;
		while(current != null)
		{
			if(current.element == x)		
				return true;
			else if(current.element > x)
				current = current.left;		
			else
				current = current.right;
		}
		return false;
	}
	
	public Node insert(int x){
		try{
			boolean exists = this.contains(x);
			if(exists)
				throw new IllegalArgumentException("Error: Already Exists");
		}
		catch(IllegalArgumentException e){System.err.println(e);}
		Node newNode = new Node(x);
		if(root == null)
		{
			root = newNode;
			return newNode;
		}
		else
		{
			Node current = root;
			Node parent = null;
			while(true)
			{
				parent = current;
				if(x < current.element)
				{				
					current = current.left;
					if(current == null)
					{
						parent.left = newNode;
						return newNode;
					}
				}
				else
				{
					current = current.right;
					if(current == null)
					{
						parent.right = newNode;
						return newNode;
					}
				}
			}
		}
	}
	
	public Node move(Node n, Node d)
	  {
	     if (n.right == null) 
	     {
	       d.element = n.element;
	       return n.left;  
	     }
	     else 
	     {
	       n.right = move(n.right, d);
	       return n;
	     }
	  }
	  
	  
    public Node delete(int x)
    {
    	Node n = this.root;
    	while (true)
    	{
		    if(n == null)
		    	return null;
		    else if (n.element == x) 
	        {
	           if (n.left == null)
	           {
	        	   n.right = move(n.right, n);
	               return n;
	           }
	           else
	           { 
	        	   n.left = move(n.left, n);
	        	   return n;
	           }
	        }
		    else
		    {
		        if (x < n.element)
		         n = n.left;
		        else
		         n = n.right;

		    }
    	}
    }
    
    public static void main(String args[])
    {
    	FineLockTree f = new FineLockTree();
    	f.insert(9);
    	f.insert(10);
    	f.insert(8);
    	f.insert(6);
    	f.insert(4);
    	f.insert(7);
    	f.insert(11);
    	f.insert(12);
    	System.out.println(f.delete(9));
    	System.out.println(f.root.left.right.element);
    	
    }
}