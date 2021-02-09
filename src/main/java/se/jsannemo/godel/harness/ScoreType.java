package se.jsannemo.godel.harness;

public enum ScoreType {
  CYCLES("Cycles", ScoreObjective.MIN),
  MEMORY("Memory cells", ScoreObjective.MIN),
  BINSIZE("Binary size", ScoreObjective.MIN);

  private final String name;
  private final ScoreObjective objective;

  ScoreType(String name, ScoreObjective objective) {
    this.name = name;
    this.objective = objective;
  }

  public String displayName() {
    return name;
  }

  public ScoreObjective getObjective() {
    return objective;
  }
}
