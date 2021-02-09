package se.jsannemo.godel.harness;

public final class Stats {

  public Stats(int instructions, int maxMemory) {
    this.instructions = instructions;
    this.maxMemory = maxMemory;
  }

  public Stats() {
    this(0, 0);
  }

  private final int instructions;
  private final int maxMemory;

  public int getInstructions() {
    return instructions;
  }

  public int getMaxMemory() {
    return maxMemory;
  }

  public Stats max(Stats other) {
    return new Stats(
        Math.max(instructions, other.instructions), Math.max(maxMemory, other.maxMemory));
  }

  @Override
  public String toString() {
    return "Stats{" + "instructions=" + instructions + ", maxMemory=" + maxMemory + '}';
  }
}
