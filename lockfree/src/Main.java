/**
 * Created by Margret on 4/17/2017.
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;

public class Main {

  public static void main(String[] args){
   /* LockFreeTree<Integer> tree = new LockFreeTree<Integer>();
     ArrayList<Integer> elements = generateElems(10);
     treeAddTest(tree, 1, 2, elements);*/
   test1(100000, 5);
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

  /* test 1: single threaded: add elements to BST in a balanced order and compare time to execute with skip lists and Java.util.TreeSet
    inputs: numElements = number of elements to insert into BST
            numTrials = number of trials to run for each BST. Function averages the times from <numTrials> trials
    */
  public static void test1(int numElements, int numTrials){
      // Part 1: test LockFreeTree
      ArrayList<Integer> elements = generateElems(numElements);
      ArrayList<Long> addExecuteTime = new ArrayList<Long>();
      ArrayList<Long> findExecuteTime = new ArrayList<Long>();
      ArrayList<Long> remExecuteTime = new ArrayList<Long>();

      ArrayList<LockFreeTree<Integer>> trees = new ArrayList<LockFreeTree<Integer>>();
      // test adding elements
      for (int x = 0; x< numTrials; x++){
          LockFreeTree<Integer> T1tree = new LockFreeTree<Integer>();
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              T1tree.add(i);
          }
          long stopTime = System.currentTimeMillis();
          addExecuteTime.add(stopTime-startTime);

            // test finding elements
          startTime = System.currentTimeMillis();
          for (Integer i: elements){
              if (!T1tree.contains(i)){
                  System.out.println("Error finding element " + i + " in LockfreeTree in Test1");
              }
          }
          stopTime = System.currentTimeMillis();
          findExecuteTime.add(stopTime-startTime);

          //TODO: REMOVE() CAUSES PROBLEMS
          // test removing elements
          /*startTime = System.currentTimeMillis();
          for (Integer i: elements){
              //System.out.println("Removing element " + i);
              if (!T1tree.remove(i)){
                  System.out.println("Error removing element " + i + " in LockfreeTree in Test1");
              }
          }
          stopTime = System.currentTimeMillis();
          remExecuteTime.add(stopTime-startTime);*/
      }

      System.out.println("Avg time for adding " + numElements + " elements to LockFreeTree: " + avg(addExecuteTime) + " ms");
      System.out.println("Avg time for finding " + numElements + " elements to LockFreeTree: " + avg(findExecuteTime) + " ms");
      //System.out.println("Avg time for removing " + numElements + " elements to LockFreeTree: " + avg(remExecuteTime) + " ms");
      addExecuteTime.clear();
      findExecuteTime.clear();
      remExecuteTime.clear();

      // Part 2: test SkipList
      /*for (int x = 0; x< numTrials; x++){
          ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<Integer>();
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              skipList.add(i);
          }
          long stopTime = System.currentTimeMillis();
          executionTimes.add(stopTime-startTime);
      }
      System.out.println("Avg time for adding " + numElements + " elements to ConcurrentSkipList: " + avg(executionTimes) + " ms");
      executionTimes.clear();

      // Part 3: test TreeSet
      for (int x = 0; x< numTrials; x++){
          ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<Integer>();
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              skipList.add(i);
          }
          long stopTime = System.currentTimeMillis();
          executionTimes.add(stopTime-startTime);
      }
      System.out.println("Avg time for adding " + numElements + " elements to ConcurrentSkipList: " + avg(executionTimes) + " ms");
      executionTimes.clear();*/
  }

  public static double avg(ArrayList<Long> executionTimes){
      long sum = 0;
      for (Long l: executionTimes){
          System.out.print(l + " ");
          sum += l;
      }
      return (double)sum/executionTimes.size();
  }
  // adds consecutive integers between start and end to tree
  public static void treeAddTest(LockFreeTree<Integer> LFtree, int numTrials, int numThreads, ArrayList<Integer> elements){
      System.out.println("Starting");
      ExecutorService pool = Executors.newFixedThreadPool(numThreads);
      Set<Future<Long>> set = new HashSet<Future<Long>>();
      //int mult = /(end - start);
      for (int i = 0; i < numTrials; i++){
          for (int j = 0; j < numThreads; j++){
              //service = Executors.newSingleThreadExecutor();
               //make all elements unique by adding offset to elements based on their thread number
              for (int x =0 ; x < elements.size(); x++){
                  elements.set(x, elements.get(x) + 2*j * elements.size());
              }
              Callable<Long> addElemThr = new addElemTest(LFtree, elements);
              Future<Long> future = pool.submit(addElemThr);
              set.add(future);
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
      ArrayList<Integer> elements;
      //int start, end;

      public addElemTest(LockFreeTree tree, ArrayList<Integer> e){
          LFtree = tree;
          elements = e;
          /*start = st;
          end = e;*/
      }
      @Override
      public Long call(){
         boolean error = false;

          //run test
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              LFtree.add(i);
              System.out.println("Added " + i); //just for debuging - remove later!
          }
          long stopTime = System.currentTimeMillis();

          // "start over" by removing elements from tree
/*          error = false;
          for (Integer i: elements){
              if (!LFtree.remove(i)){
                  System.out.println("Error removing element: " + i);
                  error = true;
                  break;
              }
          }*/

          if (!error){
              return stopTime - startTime;
          }
          return (long)(-1);
      }
  }
   //function generates number of elements to insert into BST
  static ArrayList<Integer> generateElems(int numElements){
      //find height of binary tree
      int height = 0;
      while (numElements > Math.pow(2,height)){
          height++;
      }
      System.out.println("========generateElems=======");
      System.out.println("Tree Height = " + height);

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
      System.out.println("Below sizes should be the same:");
      System.out.println("BST array size: " + BSTList.size());
      Set<Integer> foo = new HashSet<Integer>(BSTList);
      System.out.println("Set size: " + foo.size());
      if (BSTList.size() == foo.size())
          System.out.println("Looks good to me");

      else
          System.out.println("Error in generating BST elements.");
      System.out.println("============================");
      return BSTList;
  }

}
