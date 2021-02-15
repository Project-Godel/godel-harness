package se.jsannemo.godel.harness.task;

import com.google.auto.value.AutoValue;
import se.jsannemo.godel.harness.Harness;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.taskutil.InputUtil;
import se.jsannemo.godel.harness.taskutil.VmUtil;
import se.jsannemo.spooky.vm.SpookyVm;
import se.jsannemo.spooky.vm.StdLib;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BinarySort extends Harness {

  @Override
  protected void run(Executable executable, FileInputStream inputStream) throws VmException {
    int[] input = readInput(inputStream);
    Result result = runCase(executable, input, true);
    System.out.println(
        "Resulting array: "
            + Arrays.stream(input).mapToObj(String::valueOf).collect(Collectors.joining("")));
    if (result.calledTwice) {
      System.out.println("Called getTerm(i) twice for the same value!");
    }
    System.out.println("Swaps: " + result.swaps);
    printStats(result.stats);
  }

  public static int[] readInput(InputStream is) {
    return InputUtil.readBinaryString(new Scanner(is));
  }

  public static Result runCase(Executable exec, int[] input, boolean debug) throws VmException {
    Result result = new Result();
    boolean[] seen = new boolean[input.length];
    SpookyVm vm =
        VmUtil.base(exec, debug, 15)
            .addExtern("terms", (v) -> StdLib.setReturn(v, 0, input.length))
            .addExtern(
                "term",
                (v) -> {
                  int idx = StdLib.getArg(v, 0);
                  if (idx < 0 || idx >= input.length) {
                    throw new VmException("getTerm out of bounds");
                  }
                  if (seen[idx]) {
                    result.calledTwice = true;
                  }
                  seen[idx] = true;
                  StdLib.setReturn(v, 1, input[idx]);
                })
            .addExtern(
                "swap",
                (v) -> {
                  int i = StdLib.getArg(v, 0);
                  int j = StdLib.getArg(v, 1);
                  if (i < 0 || j < 0 || i >= input.length || j >= input.length) {
                    throw new VmException("swap out of bounds");
                  }
                  int val = input[i];
                  input[i] = input[j];
                  input[j] = val;
                  result.swaps++;
                })
            .build();
    VmUtil.tickOrTle(vm, 30000);
    result.stats = new Stats(vm.getInstructions(), vm.getMaxMemory());
    return result;
  }

  @AutoValue
  public static class Result {
    public Stats stats;
    public boolean calledTwice;
    public int swaps;
  }
}
