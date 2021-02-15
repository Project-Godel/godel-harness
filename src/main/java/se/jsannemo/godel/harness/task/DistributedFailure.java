package se.jsannemo.godel.harness.task;

import se.jsannemo.godel.harness.Harness;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.distributed.Distributor;
import se.jsannemo.godel.harness.taskutil.InputUtil;
import se.jsannemo.spooky.vm.StdLib;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class DistributedFailure extends Harness {
  @Override
  public void run(Executable executable, FileInputStream inputStream) throws VmException {
    Result result = runCase(executable, readInput(inputStream), true);
    System.out.println("output(): " + result.outputs);
    printStats(result.stats);
  }

  public static Input readInput(InputStream is) {
    Scanner insc = new Scanner(is);
    Input input = new Input();
    input.input = InputUtil.readLenPrefixedInts(insc);
    input.failure = insc.nextInt();
    return input;
  }

  public static Result runCase(Executable exec, Input input, boolean debug) throws VmException {
    boolean[][] correctCall = new boolean[3][input.input.length];
    boolean[] broken = new boolean[3];
    Result result = new Result();
    Distributor distributor =
        Distributor.newBuilder(exec, 3, 100000)
            .addStdLib(!debug)
            .addExtern(
                "term",
                (vm, node) -> {
                  int i = StdLib.getArg(vm, 0);
                  if (i < 0 || i >= input.input.length) {
                    throw new VmException("Out of bounds term: " + i);
                  }
                  if (!broken[node] || correctCall[node][i]) {
                    correctCall[node][i] = true;
                    StdLib.setReturn(vm, 1, input.input[i]);
                  } else {
                    StdLib.setReturn(vm, 1, 0);
                  }
                  if (i == input.failure) {
                    broken[node] = true;
                  }
                })
            .addOutput("output", result.outputs)
            .build();
    result.stats = distributor.run();
    return result;
  }

  public static class Input {
    int[] input;
    int failure;
  }

  private static class Result {
    Stats stats;
    ArrayList<Integer> outputs;
  }
}
