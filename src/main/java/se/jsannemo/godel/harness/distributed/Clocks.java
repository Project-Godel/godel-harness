package se.jsannemo.godel.harness.distributed;

import static com.google.common.base.Preconditions.checkArgument;

/** Keeps track of wall-clock times of a set of computation nodes. */
final class Clocks {

  private int[] times;

  public Clocks(int nodes) {
    this.times = new int[nodes];
  }

  /** The current time of {@code node}. */
  public int time(int node) {
    return times[node];
  }

  /** Called when {@code time} time has passed for {@code node}. */
  public void tick(int node, int time) {
    checkArgument(time >= 0);
    times[node] += time;
  }

  /** Must be called when {@code node} is acting upon an event that happened at {@code time}. */
  public void causality(int node, int time) {
    times[node] = Math.max(times[node], time);
  }
}
