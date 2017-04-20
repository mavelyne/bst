/**
 * Created by Margret on 4/17/2017.
 */
package src;

import java.util.*;
import java.util.concurrent.*;

public class Main {
  public static void main(String[] args){
    LockFreeTree<Integer> tree = new LockFreeTree<Integer>();

    //treeAddTest(tree, 1, 8, 0,10000);
     ArrayList<Integer> elements = insertBalanced(100000);
    /*int mid = 5;
    int len = 2;

    System.out.println("Starting");

    for(int i = 0; (mid + i < mid + len); i++){
      boolean result = tree.add(i);
      if(result){
        System.out.println("Added " + i);
      }else{
        System.out.println("  Did not add " + i);
      }
      result = tree.contains(i);
      if(result){
        System.out.println("Contains " + i);
      }else{
        System.out.println("  Does not add " + i);
      }
    }

    for(int i = 0; (mid + i < mid + len); i++){
      boolean result = tree.remove(i);
      if(result){
        System.out.println("Removed " + i);
      }else{
        System.out.println("  Did remove add " + i);
      }
      result = tree.contains(i);
      if(result){
        System.out.println("  Contains " + i);
      }else{
        System.out.println("Does not contain " + i);
      }
    }

    System.out.println("Done");*/
  }

  // adds consecutive integers between start and end to tree
    // note: end must be greater than start
  public static void treeAddTest(LockFreeTree<Integer> LFtree, int numTrials, int numThreads, int start, int end){
      System.out.println("Starting");
      //final ExecutorService service;
      //final Future<Long> timeTaken;
      //ArrayList<Future<Long>> results= new ArrayList<Future<Long>>();
      ExecutorService pool = Executors.newFixedThreadPool(numThreads);
      Set<Future<Long>> set = new HashSet<Future<Long>>();
      //int mult = /(end - start);
      for (int i = 0; i < numTrials; i++){
          for (int j = 0; j < numThreads; j++){
              //service = Executors.newSingleThreadExecutor();
              Callable<Long> addElemThr = new addElemTest(LFtree, start, end);
              Future<Long> future = pool.submit(addElemThr);
              set.add(future);
              end = j * end;
              start = end;
              //results.add(service.submit(new addElemTest(LFtree, start, end)));
          }

      }
      for (Future<Long> time: set) {
          try {
              System.out.println(time.get());
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
      pool.shutdown();
      System.out.println("Done");
      //service.shutdown();

   /*   //int numTrials = 10;
      //long startTime;
      long[] execTimes = new long[numTrials];  //holds the amount of time it took to execute the function
      boolean error = false;

       //run test <numTrials> amount of times
      for (int x = 0; x < numTrials; x++){
          long startTime = System.currentTimeMillis();
          for (int i = start; i < end; i++){
              LFtree.add(i);
              //System.out.println("Added " + i);
          }
          long stopTime = System.currentTimeMillis();
          execTimes[x] = stopTime - startTime;

          // "start over" by removing elements from tree
          error = false;
          for (int i = start; i < end; i++){
              if (!LFtree.remove(i)){
                  System.out.println("Error removing element: " + i);
                  error = true;
                  break;
              }
          }
      }

      if (!error){
           // find average time for <numTrial> runs
          int sumTimes = 0;
          for (int y = 0; y < numTrials; y++){
              sumTimes += execTimes[y];
          }
          System.out.println("Average time to execute: " + (double)sumTimes / numTrials);
          System.out.println("Done");
      }*/
  }

  public static class addElemTest implements Callable<Long> {
      LockFreeTree LFtree;
      int start, end;

      public addElemTest(LockFreeTree tree, int st, int e){
          LFtree = tree;
          start = st;
          end = e;
      }
      @Override
      public Long call(){
         boolean error = false;

          //run test
          long startTime = System.currentTimeMillis();
          for (int i = start; i < end; i++){
              LFtree.add(i);
              //System.out.println("Added " + i);
          }
          long stopTime = System.currentTimeMillis();

          // "start over" by removing elements from tree
          error = false;
          for (int i = start; i < end; i++){
              if (!LFtree.remove(i)){
                  System.out.println("Error removing element: " + i);
                  error = true;
                  break;
              }
          }

          if (!error){
              return stopTime - startTime;
          }
          return (long)(-1);
      }
  }
   //function generates number of elements to insert into BST
  static ArrayList<Integer> insertBalanced(int numElements){
      //find height of binary tree
      int height = 0;
      while (numElements > Math.pow(2,height)){
          height++;
      }
      System.out.println("Height = " + height);

      ArrayList<Integer> BSTList = new ArrayList<Integer>(numElements);
      int[] BSTorder = new int[numElements + 1];
      BSTorder[1] = numElements / 2; //set up heap-like array, where left children are 2i from parent, right children are 2i + 1
      int offset = (int)Math.pow(2,(height - 2));
      int multiple = 2;
      int x = 0;
      for(int i = 2; i < numElements + 1; i++){
          if (x >= multiple){
              x = 0;
              multiple *= 2;
              offset/= 2;
          }

          if (i % 2 == 1) //right child
              BSTorder[i] = BSTorder[(i - 1)/2] + offset;

          else // left child
              BSTorder[i] = BSTorder[i/2] - offset;

          x++;
      }
      for (Integer i: BSTorder){
          System.out.print(i+ ", ");
          BSTList.add(i);
      }

      //test for duplicate elements in arraylist
      BSTList.remove(0);
      System.out.println("\nBST array size: " + BSTList.size());
      Set<Integer> foo = new HashSet<Integer>(BSTList);
      System.out.println("Set size: " + foo.size());

      return BSTList;
  }

 /*  //recursive function to insert elements into BST in order
  static void balancedHelper(int point, int numLeft){
     if (numLeft == 0){
         //System.out.println("Added " + midpt);
          return;
     }
     else{
         System.out.println("Added " + (point + numLeft/2));
         System.out.println("Added " + (point - numLeft/2));
         balancedHelper(point + numLeft/2, numLeft -= 1);
         balancedHelper(point - numLeft/2, numLeft -= 1);
     }
     else{
          System.out.println("Added " + midpt);
          numLeft--;
          height --;
          return balancedHelper(midpt + (int)Math.pow(2, height -1),height/2, numLeft);
          numLeft--;
          return balancedHelper(midpt - (int)Math.pow(2, height -1),height/2, numLeft);
      }


  }*/

}
