package se.jsannemo.godel.harness.task;

import se.jsannemo.godel.harness.Harness;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.distributed.Distributor;
import se.jsannemo.godel.harness.taskutil.InputUtil;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class DistributedMajority extends Harness {

  @Override
  protected void run(Executable executable, FileInputStream inputStream) throws VmException {
    Result result = runCase(executable, readInput(inputStream), true);
    System.out.println("output(): " + result.outputs);
    printStats(result.stats);
  }

  public static int[] readInput(InputStream is) {
    return InputUtil.readLenPrefixedInts(new Scanner(is));
  }

  public static Result runCase(Executable exec, int[] input, boolean debug) throws VmException {
    Result result = new Result();
    Distributor distributor =
        Distributor.newBuilder(exec, 100, 30000)
            .setMemorySize(1000)
            .addArray("term", input)
            .addOutput("output", result.outputs)
            .addStdLib(!debug)
            .build();
    result.stats = distributor.run();
    return result;
  }

  public static class Result {
    public Stats stats;
    public ArrayList<Integer> outputs = new ArrayList<>();
  }
}
