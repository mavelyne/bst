import java.util.*;
import java.util.concurrent.*;

public class FineLockTester {

  private static final boolean DEBUG = false;

  public static void main(String[] args){
   /* FineLockTree<Integer> tree = new FineLockTree<Integer>();
     ArrayList<Integer> elements = generateElems(10);
     treeAddTest(tree, 1, 2, elements);*/

      // single threaded test
      System.out.println("\nRunning Single Threaded Tests");
      test1(100000, 5);

      // low contention test
      System.out.println("\nRunning Low Contention Tests");
      test2(100000, true);

      //TODO: High Contention Tests
      System.out.println("\nRunning High Contention Tests");
      test2(10, false);
  }

	/**
   * test2: Low Contention test using four threads
   * @param numElements
     @param isTest2: true will run test2, false will run test3
   */
  public static void test2(int numElements, boolean isTest2){
      Random rand = new Random();
      Collection<Integer> tree = new FineLockTree<Integer>();
      Thread thread1, thread2, thread3, thread4;
      List<Long> t1 = new ArrayList<Long>(), t2 = new ArrayList<Long>(), t3 = new ArrayList<Long>(), t4 = new ArrayList<Long>();
      List<Integer> e1 = new ArrayList<Integer>(), e2 = new ArrayList<Integer>(), e3 = new ArrayList<Integer>(), e4 = new ArrayList<Integer>();

      if (isTest2){
          int max = numElements * 100;
          int q2 = numElements / 2;
          int q1 = q2 / 2;
          int q3 = q2 + q1;

          // initialize elements to count
          e1.add(0);
          e2.add(q1);
          e3.add(q2);
          e4.add(q3);
          for(int i = 0; i < numElements/4; i++){
              e1.add(rand.nextInt(q1));
              e2.add(rand.nextInt(q1) + q1);
              e3.add(rand.nextInt(q1) + q2);
              e4.add(rand.nextInt(q1) + q3);
          }
      }
      else{
          ArrayList<Integer> elems = generateElems(numElements,false);
          for (int i =0; i < numElements/4; i++){
              e1.add(elems.get(rand.nextInt(elems.size())));
              e2.add(elems.get(rand.nextInt(elems.size())));
              e3.add(elems.get(rand.nextInt(elems.size())));
              e4.add(elems.get(rand.nextInt(elems.size())));
          }
      }

      // test for add
      tree = new FineLockTree<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.INSERT);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.INSERT);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.INSERT);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.INSERT);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      double average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for adding an element to FineLockTree: " + average + " ns");

      tree = new ConcurrentSkipListSet<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.INSERT);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.INSERT);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.INSERT);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.INSERT);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for adding an element to ConcurrentSkipList: " + average + " ns");

      // test for contains
      tree = new FineLockTree<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.CONTAINS);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.CONTAINS);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.CONTAINS);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.CONTAINS);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for finding an element to FineLockTree: " + average + " ns");

      tree = new ConcurrentSkipListSet<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.CONTAINS);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.CONTAINS);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.CONTAINS);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.CONTAINS);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for finding an element to ConcurrentSkipListSet: " + average + " ns");

      // test for remove
      tree = new FineLockTree<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.DELETE);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.DELETE);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.DELETE);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.DELETE);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for removing an element to FineLockTree: " + average + " ns");

      tree = new ConcurrentSkipListSet<Integer>();
      thread1 = new TreeOpThread<Integer>(tree, e1, t1, TreeOpThread.Op.DELETE);
      thread2 = new TreeOpThread<Integer>(tree, e2, t2, TreeOpThread.Op.DELETE);
      thread3 = new TreeOpThread<Integer>(tree, e3, t3, TreeOpThread.Op.DELETE);
      thread4 = new TreeOpThread<Integer>(tree, e4, t4, TreeOpThread.Op.DELETE);

      thread1.start(); thread2.start(); thread3.start(); thread4.start();
      try {
          thread1.join(); thread2.join(); thread3.join(); thread4.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      t1.addAll(t2); t1.addAll(t3); t1.addAll(t4);
      average = avg(t1, DEBUG); // multiply by 1000 to get ms from nanoseconds
      System.out.println("Avg time for removing an element to ConcurrentSkipListSet: " + average + " ns");

  }

	/**
   * Runs a specified operation on a tree for all the elements given and logs the times
    * @param <T>
   */
  public static class TreeOpThread<T> extends Thread{
      Collection<T> tree;
      List<T> elements;
      List<Long> times;
      public enum Op {INSERT, CONTAINS, DELETE};
      Op op;

      public TreeOpThread(Collection<T> tree, List<T> elements, List<Long> times, Op op) {
          this.tree = tree;
          this.elements = elements;
          this.op = op;
          this.times = times;
          if(op != Op.INSERT){
              for(T element : this.elements){
                  this.tree.add(element);
              }
          }
      }

      @Override
      public void run(){
          long start, end;
          for(T element : elements){
              start = System.nanoTime();
              switch(this.op){
                  case INSERT:
                      this.tree.insert(element);
                      break;
                  case DELETE:
                      this.tree.remove(element);
                      break;
                  case CONTAINS:
                      this.tree.contains(element);
                      break;
              }
              end = System.nanoTime();
              this.times.add(end-start);
          }
      }

  }

  /**
   * test 1: single threaded: add elements to BST in a balanced order and compare time to execute with skip lists and Java.util.TreeSet
   * inputs: numElements = number of elements to insert into BST
             numTrials = number of trials to run for each BST. Function averages the times from <numTrials> trials
  */
  public static void test1(int numElements, int numTrials){
      // Part 1: test FineLockTree
      ArrayList<Integer> elements = generateElems(numElements, false);
      ArrayList<Long> addExecuteTime = new ArrayList<Long>();
      ArrayList<Long> findExecuteTime = new ArrayList<Long>();
      ArrayList<Long> remExecuteTime = new ArrayList<Long>();

      ArrayList<FineLockTree<Integer>> trees = new ArrayList<FineLockTree<Integer>>();
      // test adding elements
      for (int x = 0; x< numTrials; x++){
          FineLockTree<Integer> T1tree = new FineLockTree<Integer>();
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
                  System.out.println("Error finding element " + i + " in FineLockTree in Test1");
              }
          }
          stopTime = System.currentTimeMillis();
          findExecuteTime.add(stopTime-startTime);

          // test removing elements
          startTime = System.currentTimeMillis();
          Collections.reverse(elements);
          for (Integer i: elements){
              //System.out.println("Removing element " + i);
              if (!T1tree.remove(i)){
                  System.out.println("Error removing element " + i + " in FineLockTree in Test1");
              }
          }
          stopTime = System.currentTimeMillis();
          remExecuteTime.add(stopTime-startTime);
          Collections.reverse(elements);
      }

      System.out.println("Avg time for adding an element to FineLockTree: " + avg(addExecuteTime, DEBUG)/numElements*1000 + " ns");
      System.out.println("Avg time for finding an element to FineLockTree: " + avg(findExecuteTime, DEBUG)/numElements*1000 + " ns");
      System.out.println("Avg time for removing an element to FineLockTree: " + avg(remExecuteTime, DEBUG)/numElements*1000 + " ns");
      addExecuteTime.clear();
      findExecuteTime.clear();
      remExecuteTime.clear();

      // Part 2: test SkipList
      ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<Integer>();
      for (int x = 0; x< numTrials; x++){
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              skipList.add(i);
          }
          long stopTime = System.currentTimeMillis();
          addExecuteTime.add(stopTime-startTime);
      }
      System.out.println("Avg time for adding an element to ConcurrentSkipList: " + avg(addExecuteTime, DEBUG)/numElements*1000 + " ns");
      addExecuteTime.clear();

      for (int x = 0; x< numTrials; x++){
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              skipList.contains(i);
          }
          long stopTime = System.currentTimeMillis();
          findExecuteTime.add(stopTime-startTime);
      }
      System.out.println("Avg time for finding an element to ConcurrentSkipList: " + avg(findExecuteTime, DEBUG)/numElements*1000 + " ns");
      findExecuteTime.clear();

      for (int x = 0; x< numTrials; x++){
          long startTime = System.currentTimeMillis();
          for (Integer i: elements){
              skipList.remove(i);
          }
          long stopTime = System.currentTimeMillis();
          remExecuteTime.add(stopTime-startTime);
      }
      System.out.println("Avg time for removing an element to ConcurrentSkipList: " + avg(remExecuteTime, DEBUG)/numElements*1000 + " ns");
      remExecuteTime.clear();
  }

  public static double avg(List<Long> executionTimes, boolean print){
      long sum = 0;
      for (Long l: executionTimes){
          if(print) System.out.print(l + " ");
          sum += l;
      }
      if(print) System.out.print("\t");
      return (double)sum/executionTimes.size();
  }

  // adds consecutive integers between start and end to tree
  public static void treeAddTest(FineLockTree<Integer> FLTree, int numTrials, int numThreads, ArrayList<Integer> elements){
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
              Callable<Long> addElemThr = new addElemTest(FLTree, elements);
              Future<Long> future = pool.submit(addElemThr);
              set.add(future);
              //results.add(service.submit(new addElemTest(FLTree, start, end)));
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
              FLTree.add(i);
              //System.out.println("Added " + i);
          }
          long stopTime = System.currentTimeMillis();
          execTimes[x] = stopTime - startTime;
          // "start over" by removing elements from tree
          error = false;
          for (int i = start; i < end; i++){
              if (!FLTree.remove(i)){
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
      FineLockTree FLTree;
      ArrayList<Integer> elements;
      //int start, end;

      public addElemTest(FineLockTree tree, ArrayList<Integer> e){
          FLTree = tree;
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
              FLTree.add(i);
              System.out.println("Added " + i); //just for debuging - remove later!
          }
          long stopTime = System.currentTimeMillis();

          // "start over" by removing elements from tree
/*          error = false;
          for (Integer i: elements){
              if (!FLTree.remove(i)){
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

  /**
   * generates number of elements to insert into BST
   * @param numElements
   * @return
   */
  static ArrayList<Integer> generateElems(int numElements, boolean print){
      //find height of binary tree
      int height = 0;
      while (numElements > Math.pow(2,height)){
          height++;
      }
      if(print){
          System.out.println("========generateElems=======");
          System.out.println("Tree Height = " + height);
      }

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
          if(print) System.out.print(i+ ", ");
          BSTList.add(i);
      }

      //test for duplicate elements in arraylist
      BSTList.remove(0);
      Set<Integer> foo = new HashSet<Integer>(BSTList);

      if(print){
          System.out.println("Below sizes should be the same:");
          System.out.println("BST array size: " + BSTList.size());
          System.out.println("Set size: " + foo.size());
      }

      if (BSTList.size() == foo.size()) {
          if(print) System.out.println("Looks good to me");
      }else{
          if(print) System.out.println("Error in generating BST elements.");
      }

      if(print) System.out.println("============================");

      return BSTList;
  }

}
