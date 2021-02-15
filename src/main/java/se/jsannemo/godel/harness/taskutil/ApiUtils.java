package se.jsannemo.godel.harness.taskutil;

import se.jsannemo.spooky.vm.SpookyVm;
import se.jsannemo.spooky.vm.StdLib;
import se.jsannemo.spooky.vm.VmException;

import java.util.ArrayList;

public final class ApiUtils {

  public static void addStream(SpookyVm.Builder vmBuilder, String streamName, int[] stream) {
    int[] term = new int[1];
    vmBuilder.addExtern(
        streamName,
        (vm) -> {
          if (term[0] == stream.length) {
            throw new VmException("Stream " + streamName + " out of bounds");
          }
          StdLib.setReturn(vm, 0, stream[term[0]++]);
        });
    vmBuilder.addExtern(
        "has" + Character.toUpperCase(streamName.charAt(0)) + streamName.substring(1),
        (vm) -> StdLib.setReturn(vm, 0, term[0] == stream.length ? 0 : 1));
  }

  public static void addArray(SpookyVm.Builder vmBuilder, String arrayName, int[] array) {
    vmBuilder.addExtern(arrayName + "s", (vm) -> StdLib.setReturn(vm, 0, array.length));
    addArrayWithoutCount(vmBuilder, arrayName, array);
  }

  public static void addArrayWithoutCount(
      SpookyVm.Builder vmBuilder, String arrayName, int[] array) {
    vmBuilder.addExtern(
        arrayName,
        (vm) -> {
          int i = StdLib.getArg(vm, 0);
          if (i < 0 || i >= array.length) {
            throw new VmException("Accessing out-of-bounds " + arrayName + " " + i);
          }
          StdLib.setReturn(vm, 1, array[i]);
        });
  }

  public static void addOutput(
      SpookyVm.Builder vmBuilder, String name, ArrayList<Integer> outputs) {
    vmBuilder.addExtern(name, (vm) -> outputs.add(StdLib.getArg(vm, 0)));
  }
}
