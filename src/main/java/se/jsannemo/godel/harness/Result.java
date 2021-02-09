package se.jsannemo.godel.harness;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkArgument;

@AutoValue
public abstract class Result {
    public abstract ImmutableMap<ScoreType, Double> scoreTypes();

    public abstract Outcome outcome();

    public static Result createAccepted(ImmutableMap<ScoreType, Double> score) {
        return new AutoValue_Result(score, Outcome.ACCEPTED);
    }

    public static Result createFailed(Outcome outcome) {
        checkArgument(outcome != Outcome.ACCEPTED, "Creating failed result with accepted outcome");
        return new AutoValue_Result(ImmutableMap.of(), outcome);
    }

    public boolean isAccepted() {
        return outcome() == Outcome.ACCEPTED;
    }

    public enum Outcome {
        ACCEPTED("Accepted"), CYCLES_EXCEEDED("Cycles exceeded"), WRONG_ANSWER("Wrong answer"), RUNTIME_ERROR("Run-time error");

        private String name;

        Outcome(String name) {
            this.name = name;
        }

        public String displayName() {
            return switch (this) {
                case ACCEPTED -> "Accepted";
                case CYCLES_EXCEEDED -> "Cycle limit exceeded";
                case WRONG_ANSWER -> "Wrong answer";
                case RUNTIME_ERROR -> "Run-time error";
            };
        }
    }
}
