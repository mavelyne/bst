import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Margret on 4/13/2017.
 * Implementation of Howley and Jones Non-Blocking Internal Binary Search Tree
 * Marked references are "NULL" references as described in algorithm to avoid ABA problems
 */
public class LockFreeTree<T extends Comparable<T>> {

  Node root; // right child is the head of the tree

  public LockFreeTree(){
    root = new Node(null);
  }

  /**
   * TODO
   * @param key
   * @return
   */
  public boolean contains(T key){
    boolean retval = false;

    return retval;
  }

  /**
   * TODO
   * @param key
   * @return
   */
  public boolean add(T key){
    boolean retval = false;

    return retval;
  }

  /**
   * TODO
   * @param key
   * @return
   */
  public boolean remove(T key){
    boolean retval = false;

    return retval;
  }

  //TODO
  private FindResult find(T key, AtomicMarkableReference<Node> pred,
                          AtomicReference<Operation> predOp,
                          AtomicMarkableReference<Node> curr,
                          AtomicReference<Operation> currOp,
                          AtomicMarkableReference<Node> auxRoot){
    FindResult retval;
    FindResult.Status retStatus = FindResult.Status.NOTFOUND_R;
    T currKey;
    AtomicMarkableReference<Node> next, lastRight;
    AtomicReference<Operation> lastRightOp;
    boolean retry = true; // so loop goes through once! (probs should change to do-while loop

    while(retry){
      retry = false;
      retStatus = FindResult.Status.NOTFOUND_R;
      curr = auxRoot;
      currOp = (curr.getReference()).getOperation();
      if(!(currOp.get()).isNone()) { // if there is an operation on current node
        if(auxRoot.getReference() == this.root){ // and if starting at the actual root of the BST
          helpChildCAS(currOp, curr.getReference());
          retry = true; // should be equivalent to goto retry
          continue;
        }else{
          retval = new FindResult(FindResult.Status.ABORT,null,null,null,null);
          return retval;
        }
      }

      next = curr.getReference().getRight();
      lastRight = curr;
      lastRightOp = currOp;

      while(! next.isMarked()){ //i.e. while the next node is not NULL
        pred = curr;
        predOp = currOp;
        curr = next;
        currOp = curr.getReference().getOperation();

        if(! currOp.get().isNone()){
          help(pred, predOp, curr, currOp);
          retry = true;
          continue;
        }
        retry = false;

        AtomicReference<T> temp = curr.getReference().getKey();
        currKey = temp.get();
        int compare = key.compareTo(currKey);
        if(compare < 0){
          retStatus = FindResult.Status.NOTFOUND_L;
          next = curr.getReference().getLeft();
        }else if(compare > 0){
          retStatus = FindResult.Status.NOTFOUND_R;
          next = curr.getReference().getRight();
          lastRight = curr;
          lastRightOp = currOp;
        }else{
          retStatus = FindResult.Status.FOUND;
          break;
        }
      }

      AtomicReference<Operation> temp = lastRight.getReference().getOperation();
      if((retStatus != FindResult.Status.FOUND) &&
          (lastRightOp.get() != temp.get())){
        retry = true;
        continue;
      }

      if(curr.getReference().getOperation().get() != currOp){
        retry = true;
        continue;
      }

    } // end of retry loop

    retval = new FindResult(retStatus, pred, curr, predOp, currOp);
    return retval;
  }

  private void help(AtomicMarkableReference<Node> pred, AtomicReference<Operation> predOp,
                    AtomicMarkableReference<Node> curr, AtomicReference<Operation> currOp){
    // TODO
  }

  private void helpChildCAS(AtomicReference<Operation> opRef, Node dest){
    AtomicMarkableReference<Node> address;
    ChildCASOperation op = (ChildCASOperation) opRef.get();
    if(op.isLeft()){
      address = dest.getLeft();
    }else{
      address = dest.getRight();
    }
    boolean mark;
    address.compareAndSet(op.getExpected(), op.getUpdate(), address.isMarked(), false);
    dest.getOperation().compareAndSet(op, new NoneOperation());
  }

  private static class Node<T>{
    AtomicMarkableReference<Node> left, right; // marked true if NULL
    volatile AtomicReference<T> key;
    volatile AtomicReference<Operation> op;

    /**
     * Initialize node with no children for a given key
     * @param key
     */
    public Node(T key){
      this.key = new AtomicReference<T>(key);
      this.left = new AtomicMarkableReference<Node>(null, true);
      this.right = new AtomicMarkableReference<Node>(null, true);
      this.op = new AtomicReference<Operation>(new NoneOperation());
    }

    public AtomicMarkableReference<Node> getLeft() {
      return left;
    }

    public AtomicMarkableReference<Node> getRight() {
      return right;
    }

    public AtomicReference<T> getKey() {
      return key;
    }

    public AtomicReference<Operation> getOperation() {
      return op;
    }
  }

  /**
    Because result of a find needs to return multiple things
   */
  private static class FindResult{
    public enum Status{FOUND, NOTFOUND_L,NOTFOUND_R, ABORT};
    private Status status;
    private AtomicMarkableReference<Node> pred, curr;
    private AtomicReference<Operation> predOp, currOp;

    public FindResult(Status status, AtomicMarkableReference<Node> pred, AtomicMarkableReference<Node> curr, AtomicReference<Operation> predOp, AtomicReference<Operation> currOp) {
      this.status = status;
      this.pred = pred;
      this.curr = curr;
      this.predOp = predOp;
      this.currOp = currOp;
    }

    public Status getStatus() {
      return status;
    }

    public AtomicMarkableReference<Node> getPred() {
      return pred;
    }

    public AtomicMarkableReference<Node> getCurr() {
      return curr;
    }

    public AtomicReference<Operation> getPredOp() {
      return predOp;
    }

    public AtomicReference<Operation> getCurrOp() {
      return currOp;
    }
  }

  /**
    Operation classes keep track of what operation is being performed on the node
   */
  private static abstract class Operation{
    boolean isNone(){return false;}
    boolean isMarked(){return false;}
    boolean isChildCAS(){return false;}
    boolean isRelocate(){return false;}
  }
  private static class NoneOperation extends Operation{boolean isNone(){return true;}}
  private static class MarkedOperation extends Operation{boolean isMarked(){return true;}}
  private static class ChildCASOperation extends Operation{
    private boolean isLeft; // indicates which child node is being operated on
    private Node expected;
    private Node update;
    boolean isChildCAS(){return true;}

    public ChildCASOperation(boolean isLeft, Node expected, Node update) {
      this.isLeft = isLeft;
      this.expected = expected;
      this.update = update;
    }

    public boolean isLeft() {
      return isLeft;
    }

    public Node getExpected() {
      return expected;
    }

    public Node getUpdate() {
      return update;
    }
  }
  private static class RelocateOperation extends Operation{
    public enum State{FAILED, ONGOING, SUCCESS};
    private AtomicReference<State> state;
    private Node dest;
    private Operation destOp;
    private Object removeKey;
    private Object replaceKey;
    boolean isRelocate(){return true;}

    public RelocateOperation(State state, Node dest, Operation destOp, Object removeKey, Object replaceKey) {
      this.state.set(state);
      this.dest = dest;
      this.destOp = destOp;
      this.removeKey = removeKey;
      this.replaceKey = replaceKey;
    }

    public Node getDest() {
      return dest;
    }

    public Operation getDestOp() {
      return destOp;
    }

    public Object getRemoveKey() {
      return removeKey;
    }

    public Object getReplaceKey() {
      return replaceKey;
    }

    public State getState() {
      return state.get();
    }

    public void setState(State state) {
      this.state.set(state);
    }

    public boolean compareAndSet(State expected, State update){
      return this.state.compareAndSet(expected, update);
    }
  }
}
