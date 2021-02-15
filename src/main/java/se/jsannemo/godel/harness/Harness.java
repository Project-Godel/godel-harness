package se.jsannemo.godel.harness;

import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;

public abstract class Harness {

  public void runCase(Executable executable, FileInputStream inputStream) {
    try {
      run(executable, inputStream);
    } catch (VmException rte) {
      System.err.println("Run-time error: " + rte.getMessage());
    }
  }

  protected abstract void run(Executable executable, FileInputStream inputStream)
      throws VmException;

  protected void printStats(Stats stats) {
    System.out.println("Cycles: " + stats.getInstructions());
    System.out.println("Memory: " + stats.getMaxMemory());
  }
}
