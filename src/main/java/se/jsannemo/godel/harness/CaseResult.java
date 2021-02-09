package se.jsannemo.godel.harness;

public final class CaseResult {
  public Result.Outcome outcome;
  public Stats stats;

  CaseResult(Result.Outcome outcome, Stats stats) {
    this.outcome = outcome;
    this.stats = stats;
  }

  CaseResult() {
    this(Result.Outcome.ACCEPTED, new Stats());
  }

  public CaseResult aggregate(CaseResult other) {
    return new CaseResult(
        other.outcome == Result.Outcome.ACCEPTED ? outcome : other.outcome, stats.max(other.stats));
  }

  public static CaseResult accepted(Stats stats) {
    return new CaseResult(Result.Outcome.ACCEPTED, stats);
  }

  public static CaseResult failed(Result.Outcome outcome) {
    return new CaseResult(outcome, new Stats());
  }
}
