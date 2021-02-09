package se.jsannemo.godel.harness.distributed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Handles scheduling and inter-node messaging of a set of computation nodes.
 *
 * <p>The reason these two purposes are handled by this single class is to prevent cases where nodes
 * that are blocked waiting for a messages must receive the first message that could possibly be
 * sent to it during execution.
 *
 * <p>This is important to ensure since message passing is used to ensure nodes have accurate
 * wall-time clocks, by fast-forwarding the time of the receiver node that waited for a message to
 * the time of the sender of that message (at the time of sending).
 *
 * <p>The main idea is to only schedule nodes waiting for messages from <strong>any</strong> node
 * only when all nodes are waiting for such messages; and then to pick the one that is due to
 * receive the earliest sent message.
 */
final class Scheduler {

  /** Used to signify that a node is waiting for a message from any node. */
  static final int WAITING_ANY = -1;
  /** Used to signify that a node is not currently waiting for any message. */
  static final int NOT_WAITING = -2;

  private final Random rnd = new Random();
  private final int nodes;
  /** The node that each node is waiting for a message from. */
  private final int[] blockedFor;
  /** Ordered per-recipient, per-sender queues of all unread messages. */
  private final ArrayList<HashMap<Integer, Queue<Message>>> messageQueues = new ArrayList<>();
  /** Per-recipient list of all pending messages. */
  private final ArrayList<TreeSet<Message>> allMessages = new ArrayList<>();
  /**
   * The set of the earliest message sent to each of the nodes that is currently waiting for any
   * message.
   */
  private final TreeSet<Message> earliestAnyBlockedCandidates = new TreeSet<>();
  /** Nodes that may be scheduled for execution at any time. */
  private final ArrayList<Integer> scheduled = new ArrayList<>();

  private final boolean[] isScheduled;

  public Scheduler(int nodes) {
    checkArgument(nodes >= 0);
    blockedFor = new int[nodes];
    isScheduled = new boolean[nodes];
    for (int i = 0; i < nodes; i++) {
      messageQueues.add(new HashMap<>());
      allMessages.add(new TreeSet<>());
      blockedFor[i] = NOT_WAITING;
      isScheduled[i] = true;
      scheduled.add(i);
    }
    this.nodes = nodes;
  }

  /**
   * Returns true if a message can be retrieved immediately for {@code target} from {@code source}
   * or false if {@code target} must be suspended.
   */
  public boolean receive(int target, int source) {
    if (source == WAITING_ANY) {
      receiveAny(target);
      return false;
    }
    checkArgument(target >= 0 && target < nodes);
    checkArgument(source >= 0 && source < nodes);
    HashMap<Integer, Queue<Message>> targetMap = messageQueues.get(target);
    Queue<Message> msgQueue = targetMap.get(source);
    if (msgQueue == null || msgQueue.isEmpty()) {
      blockedFor[target] = source;
      return false;
    }
    return true;
  }

  private void receiveAny(int target) {
    checkArgument(target >= 0 && target < nodes);
    blockedFor[target] = WAITING_ANY;
    TreeSet<Message> messages = allMessages.get(target);
    if (!messages.isEmpty()) {
      earliestAnyBlockedCandidates.add(messages.first());
    }
  }

  public void send(Message m) {
    int receiver = m.receiver;
    messageQueues.get(receiver).computeIfAbsent(m.sender, (k) -> new ArrayDeque<>()).add(m);
    // If the receiver was already waiting for any message, the new one may be earlier than the
    // previous earliest.
    TreeSet<Message> allForReceiver = allMessages.get(receiver);
    if (blockedFor[receiver] == WAITING_ANY) {
      if (!allForReceiver.isEmpty()) {
        earliestAnyBlockedCandidates.remove(allForReceiver.first());
      }
    }
    allForReceiver.add(m);
    if (blockedFor[receiver] == WAITING_ANY) {
      earliestAnyBlockedCandidates.add(allForReceiver.first());
    }

    // If the receiver waited for this particular sender, it may be scheduled again.
    if (blockedFor[receiver] == m.sender && !isScheduled[receiver]) {
      isScheduled[receiver] = true;
      scheduled.add(receiver);
    }
  }

  /**
   * Returns the next message for a given target and sender. Must only be called if either a {@link
   * #receive(int target, int sender)} call returned true, if the node was scheduled and the source
   * is is {@code blockedFor[target]}.
   */
  public Message nextMessage(int target, int source) {
    checkArgument(target >= 0 && target < nodes);
    if (source == WAITING_ANY) {
      Message first = earliestAnyBlockedCandidates.first();
      checkArgument(target == first.receiver, "Target was not in line for any-message call");
      return nextMessage(target, first.sender);
    }
    checkArgument(source >= 0 && source < nodes);
    Queue<Message> messageQueue = messageQueues.get(target).get(source);
    checkArgument(!messageQueue.isEmpty(), "Target " + target + " does not have pending messages.");
    Message m = messageQueue.poll();
    allMessages.get(target).remove(m);
    earliestAnyBlockedCandidates.remove(m);
    blockedFor[target] = NOT_WAITING;
    System.err.println("MSG: " + m.sender + " to " + m.receiver + " (" + m.message + ")");
    return m;
  }

  /** Returns the ID of a node that can be scheduled for execution. */
  public Optional<Integer> schedule() {
    // If we have anything ready to be scheduled, pick it.
    if (!scheduled.isEmpty()) {
      int nx = rnd.nextInt(scheduled.size());
      int which = scheduled.get(nx);
      scheduled.set(nx, scheduled.get(scheduled.size() - 1));
      scheduled.remove(scheduled.size() - 1);
      isScheduled[which] = false;
      return Optional.of(which);
    }
    // Otherwise, pick the node with the earliest pending message.
    if (!earliestAnyBlockedCandidates.isEmpty()) {
      Message message = Objects.requireNonNull(earliestAnyBlockedCandidates.first());
      return Optional.of(message.receiver);
    }
    return Optional.empty();
  }

  public int blockedFor(int node) {
    return blockedFor[node];
  }

  public static class Message implements Comparable<Message> {
    int sendTime;
    int sender;
    int receiver;
    int message;

    public Message(int sendTime, int sender, int receiver, int message) {
      this.sendTime = sendTime;
      this.sender = sender;
      this.receiver = receiver;
      this.message = message;
    }

    @Override
    public int compareTo(Message message) {
      if (sendTime != message.sendTime) return Integer.compare(sendTime, message.sendTime);
      if (sender != message.sender) return Integer.compare(sender, message.sender);
      if (receiver != message.receiver) return Integer.compare(receiver, message.receiver);
      return Integer.compare(this.message, message.message);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Message message1 = (Message) o;
      return sendTime == message1.sendTime
          && sender == message1.sender
          && receiver == message1.receiver
          && message == message1.message;
    }

    @Override
    public int hashCode() {
      return Objects.hash(sendTime, sender, receiver, message);
    }

    @Override
    public String toString() {
      return "Message{"
          + "sendTime="
          + sendTime
          + ", sender="
          + sender
          + ", receiver="
          + receiver
          + ", message="
          + message
          + '}';
    }
  }
}
