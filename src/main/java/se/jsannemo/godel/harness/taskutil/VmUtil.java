package se.jsannemo.godel.harness.taskutil;

import se.jsannemo.godel.harness.TimeLimitException;
import se.jsannemo.spooky.vm.SpookyVm;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.OutputStream;
import java.io.PrintStream;

public final class VmUtil {

  public static SpookyVm.Builder base(Executable executable, boolean debug, int memLimit) {
    return SpookyVm.newBuilder(executable)
        .addStdLib()
        .setMemorySize(memLimit)
        .setStdOut(new PrintStream(debug ? System.err : OutputStream.nullOutputStream()));
  }

  public static void tickOrTle(SpookyVm vm, int instructions) throws VmException {
    while (instructions-- > 0) {
      if (!vm.executeInstruction()) return;
    }
    throw new TimeLimitException();
  }
}
