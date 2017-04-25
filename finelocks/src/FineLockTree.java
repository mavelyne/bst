import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.*;

public class FineLockTree<T extends Comparable<T>> implements Collection<T>{
	
	class Node{
		T element;
		Node left;
		Node right;	
		ReentrantLock nodeLock;
		
		public Node(T x){
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
	
	public boolean contains(T x){
		Node current = root;
		topLock.lock();
		try{
			while(current != null)
			{
				if(current.element.equals(x))		
					return true;
				else if(current.element.compareTo(x) > 0)
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
	
	public Node insert(T x){
		if (contains(x))
			return null;
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
				topLock.unlock();
				current.lock();
				while(true)
				{
					if(x.compareTo(current.element) < 0)
					{
						if(current.left == null)
						{
							current.left = newNode;
							current.unlock();
							return newNode;
						}
						else
						{
							current.unlock();
							current = current.left;
						}
					}
					else
					{
						if(current.right == null)
						{
							current.right = newNode;
							current.unlock();
							return newNode;
						}
						else
						{
							current.unlock();
							current = current.right;
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
	  
	public Node delete(T x)
    {
		if (contains(x) == false)
			return null;
    	topLock.lock();
		Node ans = null;
    	Node n = this.root;
    	topLock.unlock();
		while (true)
    	{
		    if(n == null)
		    	return null;
		    else if (n.element.equals(x)) 
	        {
		       n.lock();
		       if (n.left == null && n.right == null)
		       {
		    	   ans = new Node(n.element);
		    	   n.unlock();
		    	   return ans;
		       }
	           if (n.left == null && n.right != null)
	           {
	        	   ans = new Node(n.element);
	        	   n.right = move(n.right, n);
	        	   n.unlock();
	               return ans;
	           }
	           else if (n.left != null)
	           { 
	        	   ans = new Node(n.element);
	        	   n.left = move(n.left, n);
	        	   n.unlock();
	        	   return ans;
	           }
	        }
		    else
		    {
		    	n.lock();
		        if (x.compareTo(n.element) < 0)
		        {
		        	n.unlock();
		        	n = n.left;

		        }		         
		        else
		        {
		        	n.unlock();
		        	n = n.right;
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
		return contains((T)(o));
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
		insert(e);
		return false;
	}

	@Override
	public boolean remove(Object o) {
		Node n = delete((T)(o));
		if (n != null)
			return true;
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object elem : c)
			contains(elem);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T elem : c)
			add(elem);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object elem : c)
			remove(elem);
		return true;
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