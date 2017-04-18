import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Margret on 4/13/2017.
 * Implementation of Howley and Jones Non-Blocking Internal Binary Search Tree
 * Marked references are "NULL" references as described in algorithm to avoid ABA problems
 */
public class LockFreeTree<T extends Comparable<T>> {

  AtomicMarkableReference<Node> root; // right child is the head of the tree

  public LockFreeTree(){
    Node rootNode = new Node();
    root = new AtomicMarkableReference<Node>(rootNode, false);
  }

  /**
   * @param key
   * @return
   */
  public boolean contains(T key){
    boolean retval = false;

    AtomicMarkableReference<Node> pred = null;
    AtomicMarkableReference<Node> curr = null;
    AtomicReference<Operation> predOp = new AtomicReference<Operation>(new NoneOperation());
    AtomicReference<Operation> currOp = new AtomicReference<Operation>(new NoneOperation());

    FindResult result = find(key, pred, predOp, curr, currOp, root);
    retval = (result.getStatus() == FindResult.Status.FOUND);

    return retval;
  }

  /**
   * @param key
   * @return
   */
  public boolean add(T key){
    AtomicMarkableReference<Node> pred = null;
    AtomicMarkableReference<Node> curr = null;
    Node newNode = null;
    AtomicReference<Operation> predOp = new AtomicReference<Operation>(new NoneOperation());
    AtomicReference<Operation> currOp = new AtomicReference<Operation>(new NoneOperation());
    ChildCASOperation casOp;
    FindResult result;

    while(true){
      result = find(key, pred, predOp, curr, currOp, root);
      if(result.getStatus() == FindResult.Status.FOUND) return false;
      newNode = new Node(key);
      curr = result.getCurr();
      pred = result.getPred();
      currOp = result.getCurrOp();
      predOp = result.getCurrOp();
      boolean isLeft = (result.getStatus() == FindResult.Status.NOTFOUND_L);
      Node old = (Node)(isLeft ? curr.getReference().getLeft().getReference() : curr.getReference().getRight().getReference());
      casOp = new ChildCASOperation(isLeft, old, newNode);
      if(curr.getReference().getOperation().compareAndSet(currOp.get(), casOp)){
        helpChildCAS(casOp, curr.getReference(),false);
        return true;
      }
    }
  }

  /**
   * @param key
   * @return
   */
  public boolean remove(T key){
    AtomicMarkableReference<Node> pred = null;
    AtomicMarkableReference<Node> curr = null;
    AtomicMarkableReference<Node> replace = null;
    AtomicReference<Operation> predOp = new AtomicReference<Operation>(new NoneOperation());
    AtomicReference<Operation> currOp = new AtomicReference<Operation>(new NoneOperation());
    AtomicReference<Operation> replaceOp = new AtomicReference<Operation>(new NoneOperation());
    AtomicReference<Operation> relocOp = new AtomicReference<Operation>(new NoneOperation());

    while(true){
      FindResult found = find(key, pred, predOp, curr, currOp, root);
      if(found.getStatus() != FindResult.Status.FOUND) return false;
      curr = found.getCurr();
      pred = found.getPred();
      predOp = found.getPredOp();
      currOp = found.getCurrOp();

      if(curr.getReference().getRight().isMarked() || curr.getReference().getLeft().isMarked()){
        // node has < 2 children
        if(curr.getReference().getOperation().compareAndSet(currOp.get(), new MarkedOperation())) {
          helpMarked(pred, predOp, curr);
          return true;
        }
      }else{ // node hs 2 children
        found = find(key, pred, predOp, replace, replaceOp, curr);
        pred = found.getPred();
        predOp = found.getPredOp();
        replace = found.getCurr();
        replaceOp = found.getCurrOp();
        if(found.getStatus() == FindResult.Status.ABORT ||
            (curr.getReference().getOperation().get() != currOp.get())) continue;

        relocOp.set(new RelocateOperation(curr, currOp, key, replace.getReference().getKey()));
        if(replace.getReference().getOperation().compareAndSet(replaceOp.get(), relocOp)){
          if(helpRelocate(relocOp, pred, predOp, replace)) return true;
        }
      }
    }
  }
  
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
        if(auxRoot.getReference() == this.root.getReference()){ // and if starting at the actual root of the BST
          helpChildCAS((ChildCASOperation) currOp.get(), curr.getReference(), false);
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

      if(curr.getReference().getOperation().get() != currOp.get()){
        retry = true;
        continue;
      }

    } // end of retry loop

    retval = new FindResult(retStatus, pred, curr, predOp, currOp);
    return retval;
  }

  private void help(AtomicMarkableReference<Node> pred, AtomicReference<Operation> predOp,
                    AtomicMarkableReference<Node> curr, AtomicReference<Operation> currOp){
    if(currOp.get().isChildCAS()){
      helpChildCAS((ChildCASOperation) currOp.get(), curr.getReference(), false);
    }else if(currOp.get().isRelocate()){
      helpRelocate(currOp, pred, predOp, curr);
    }else if(currOp.get().isMarked()){
      helpMarked(pred, predOp, curr);
    }
  }

  private boolean helpRelocate(AtomicReference<Operation> currOp, AtomicMarkableReference<Node> pred, AtomicReference<Operation> predOp, AtomicMarkableReference<Node> curr){
    RelocateOperation op = (RelocateOperation) currOp.get();
    RelocateOperation.State seenState = op.getState().get();

    if(seenState.equals(RelocateOperation.State.ONGOING)){
      AtomicReference<Operation> seenOp;
      AtomicMarkableReference<Node> dest = op.getDest();
      dest.getReference().getOperation().compareAndSet(op.getDestOp().get(), op);
      seenOp = dest.getReference().getOperation();
      if((seenOp.get() == op.getDestOp().get()) || (seenOp.get() == op)){
        op.getState().compareAndSet(RelocateOperation.State.ONGOING, RelocateOperation.State.SUCCESS);
        seenState = RelocateOperation.State.SUCCESS;
      }else{
        op.getState().compareAndSet(RelocateOperation.State.ONGOING, RelocateOperation.State.FAILED);
        seenState = op.getState().get();
      }
    }

    if(seenState.equals(RelocateOperation.State.SUCCESS)){
      AtomicMarkableReference<Node> destRef = op.getDest();
      destRef.getReference().getKey().compareAndSet(op.getRemoveKey(),op.getReplaceKey());
      destRef.getReference().getOperation().compareAndSet(op, new NoneOperation());
    }

    boolean result = (seenState.equals(RelocateOperation.State.SUCCESS));
    if(op.getDest().getReference() == curr.getReference()) return result;
    Operation temp;
    if(result){temp = new MarkedOperation();}
    else{temp = new NoneOperation();}
    curr.getReference().getOperation().compareAndSet(op, temp);
    if(result){
      if(op.getDest().getReference() == pred.getReference()) predOp.set(new NoneOperation());
      helpMarked(pred, predOp, curr);
    }

    return result;
  }

  private void helpChildCAS(ChildCASOperation op, Node dest, boolean mark){
    AtomicMarkableReference<Node> address;
    if(op.isLeft()){
      address = dest.getLeft();
    }else{
      address = dest.getRight();
    }
    boolean res = address.compareAndSet(op.getExpected(), op.getUpdate(), address.isMarked(), mark);
    res = (dest.getOperation()).compareAndSet(op, new NoneOperation());
  }

  private void helpMarked(AtomicMarkableReference<Node> pred, AtomicReference<Operation> predOp, AtomicMarkableReference<Node> curr){
    Node newRef;
    boolean mark = false;
    if(curr.getReference().getLeft().isMarked()){ // left is NULL
      if(curr.getReference().getRight().isMarked()){ // right is NULL
        mark = true;
        newRef = curr.getReference();
      }else{
        newRef = (Node) curr.getReference().getRight().getReference();
      }
    }else{
      newRef = (Node) curr.getReference().getLeft().getReference();
    }

    ChildCASOperation casOp = new ChildCASOperation(curr.getReference() == pred.getReference().getLeft().getReference(),
        curr.getReference(), newRef);

    if(pred.getReference().getOperation().compareAndSet(predOp.get(), casOp))
      helpChildCAS(casOp, pred.getReference(), mark);
  }

  private static class Node<T>{
    AtomicMarkableReference<Node> left, right; // marked true if NULL
    volatile AtomicReference<T> key;
    volatile AtomicReference<Operation> op;

    public Node(){
      this.key = new AtomicReference<T>(null);
      this.left = new AtomicMarkableReference<Node>(null, true);
      this.right = new AtomicMarkableReference<Node>(null, true);
      this.op = new AtomicReference<Operation>(new NoneOperation());
    }

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
    private AtomicMarkableReference<Node> dest;
    private AtomicReference<Operation> destOp;
    private Object removeKey;
    private Object replaceKey;
    boolean isRelocate(){return true;}

    public RelocateOperation(AtomicMarkableReference<Node> dest, AtomicReference<Operation> destOp, Object removeKey, Object replaceKey) {
      this.state = new AtomicReference<State>(State.ONGOING);
      this.dest = dest;
      this.destOp = destOp;
      this.removeKey = removeKey;
      this.replaceKey = replaceKey;
    }

    public AtomicReference<State> getState() {
      return state;
    }

    public AtomicMarkableReference<Node> getDest() {
      return dest;
    }

    public AtomicReference<Operation> getDestOp() {
      return destOp;
    }

    public Object getRemoveKey() {
      return removeKey;
    }

    public Object getReplaceKey() {
      return replaceKey;
    }
  }
}
