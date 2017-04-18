/**
 * Created by Margret on 4/17/2017.
 */
public class Main {
  public static void main(String[] args){
    LockFreeTree<Integer> tree = new LockFreeTree<Integer>();

    int mid = 5;
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

    System.out.println("Done");
  }
}
