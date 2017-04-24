import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.*;

public class FineLockTree<T extends Comparable<T>> implements Collection<T>{
	
	class Node{
		int element;
		Node left;
		Node right;	
		ReentrantLock nodeLock;
		
		public Node(int x){
			element = x;
			left = null;
			right = null;
			nodeLock = new ReentrantLock();
		}
		
		public void lock(){
			this.nodeLock.lock();
		}
		
		public void unlock(){
			this.nodeLock.unlock();
		}
	}
	
	private Node root;
	private ReentrantLock topLock;
	
	public FineLockTree(){
		root = null;
		topLock = new ReentrantLock();
	}
	
	public boolean contains(int x){
		Node current = root;
		topLock.lock();
		try{
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
		finally{
			topLock.unlock();
		}
		
	}
	
	public Node insert(int x){
		Node newNode = new Node(x);
		topLock.lock();
			if(root == null)
			{
				root = newNode;
				topLock.unlock();
				return newNode;
			}
			else
			{
				Node current = root;
				Node parent = null;
				topLock.unlock();
				current.lock();
				while(true)
				{
					parent = current;
					current.unlock();
					parent.lock();
					if(x < current.element)
					{				
						current = current.left;
						if(current == null)
						{
							parent.left = newNode;
							parent.unlock();
							return newNode;
						}
					}
					else
					{
						current = current.right;
						if(current == null)
						{
							parent.right = newNode;
							parent.unlock();
							return newNode;
						}
					}
					current.lock();
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
		Node ans = null;
    	Node n = this.root;
    	topLock.lock();
		while (true)
    	{
		    if(n == null)
		    	return null;
		    else if (n.element == x) 
	        {
		       n.lock();
	           if (n.left == null)
	           {
	        	   ans = new Node(n.element);
	        	   n.right = move(n.right, n);
	        	   n.unlock();
	               return ans;
	           }
	           else
	           { 
	        	   ans = new Node(n.element);
	        	   n.left = move(n.left, n);
	        	   n.unlock();
	        	   return ans;
	           }
	        }
		    else
		    {
		    	topLock.unlock();
		    	n.lock();
		        if (x < n.element)
		        {
		        	n = n.left;
		        	n.unlock();
		        }
		         
		        else
		        {
		        	n = n.right;
		        	n.unlock();
		        }
		         
		    }
    	}
    }

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}