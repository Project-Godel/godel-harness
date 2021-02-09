package se.jsannemo.godel.harness.task;

import se.jsannemo.godel.harness.Harness;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.distributed.Distributor;
import se.jsannemo.spooky.vm.StdLib;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class DistributedAdd implements Harness {
  @Override
  public void run(Executable executable, FileInputStream inputStream) {
      int[] input = readInput(inputStream);
      ArrayList<Integer> output = new ArrayList<>();
      try {
          Stats stats = runCase(executable, input, output, true);
          System.out.println("output(): " + output);
          System.out.println("Cycles: " + stats.getInstructions());
          System.out.println("Memory: " + stats.getMaxMemory());
      } catch (VmException e){
          System.err.println(e.getMessage());
      }
  }

  public static int[] readInput(InputStream is) {
    Scanner insc = new Scanner(is);
    int[] input = new int[insc.nextInt()];
    for (int i = 0; i < input.length; i++) {
      input[i] = insc.nextInt();
    }
    return input;
  }

  public static Stats runCase(Executable exec, int[] input, ArrayList<Integer> outputAnswers, boolean debug)
      throws VmException {
    Distributor distributor =
        Distributor.newBuilder(exec, 100, 30000)
            .setMemorySize(1000)
            .addExtern("getTerms", (vm, node) -> StdLib.setReturn(vm, 0, input.length))
            .addExtern(
                "getTerm",
                (vm, node) -> {
                  int i = StdLib.getArg(vm, 0);
                  if (i < 0 || i >= input.length) {
                    throw new VmException(node + " accessing out-ot-bounds term " + i);
                  }
                  StdLib.setReturn(vm, 1, input[i]);
                })
            .addExtern(
                "output",
                (vm, node) -> {
                  outputAnswers.add(StdLib.getArg(vm, 0));
                })
            .addStdLib(!debug)
            .build();
    return distributor.run();
  }
}
