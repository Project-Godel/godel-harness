package se.jsannemo.godel.harness;

import se.jsannemo.spooky.vm.VmException;

public class TimeLimitException extends VmException {
  public TimeLimitException() {
    super("Program exceeded the cycle limit");
  }
}
