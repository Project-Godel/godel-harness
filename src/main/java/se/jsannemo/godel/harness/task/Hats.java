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
import java.util.Arrays;
import java.util.Scanner;

public class Hats extends Harness {
  @Override
  protected void run(Executable executable, FileInputStream inputStream) throws VmException {
    Result result = runCase(executable, readInput(inputStream), true);
    System.out.println("output(): " + Arrays.toString(result.outputs));
    printStats(result.stats);
  }

  public static int[] readInput(InputStream is) {
    return InputUtil.readLenPrefixedInts(new Scanner(is));
  }

  public static Result runCase(Executable exec, int[] input, boolean debug) throws VmException {
    Result result = new Result();
    int[] guess = new int[input.length];
    Distributor distributor =
        Distributor.newBuilder(exec, input.length, 30000)
            .setMemorySize(1000)
            .addExtern(
                "look",
                (vm, node) -> {
                  int i = StdLib.getArg(vm, 0);
                  if (i < 1 || i >= input.length) {
                    throw new VmException(node + " accessing out-ot-bounds hat " + i);
                  }
                  StdLib.setReturn(vm, 1, input[(i + node) % input.length]);
                })
            .addExtern("guess", (vm, node) -> guess[node] = StdLib.getArg(vm, 0))
            .addStdLib(!debug)
            .build();
    result.stats = distributor.run();
    result.outputs = guess;
    return result;
  }

  public static class Result {
    public Stats stats;
    public int[] outputs;
  }
}
