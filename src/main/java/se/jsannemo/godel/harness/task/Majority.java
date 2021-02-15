package se.jsannemo.godel.harness.task;

import se.jsannemo.godel.harness.Harness;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.taskutil.ApiUtils;
import se.jsannemo.godel.harness.taskutil.InputUtil;
import se.jsannemo.godel.harness.taskutil.VmUtil;
import se.jsannemo.spooky.vm.SpookyVm;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Majority extends Harness {
  @Override
  public void run(Executable executable, FileInputStream inputStream) throws VmException {
    Result result = runCase(executable, readInput(inputStream), true);
    System.out.println("output(): " + result.outputs);
    printStats(result.stats);
  }

  public static int[] readInput(InputStream is) {
    return InputUtil.readLenPrefixedInts(new Scanner(is));
  }

  public static Result runCase(Executable exec, int[] input, boolean debug) throws VmException {
    Result result = new Result();
    SpookyVm.Builder vmBuilder = VmUtil.base(exec, debug, 20);
    ApiUtils.addStream(vmBuilder, "term", input);
    ApiUtils.addOutput(vmBuilder, "output", result.outputs);
    SpookyVm vm = vmBuilder.build();
    VmUtil.tickOrTle(vm, 30000);
    result.stats = new Stats(vm.getInstructions(), vm.getMaxMemory());
    return result;
  }

  public static class Result {
    public Stats stats;
    public ArrayList<Integer> outputs = new ArrayList<>();
  }
}
