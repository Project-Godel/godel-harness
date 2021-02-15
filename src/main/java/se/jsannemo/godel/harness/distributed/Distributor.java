package se.jsannemo.godel.harness.distributed;

import com.google.common.collect.ImmutableList;
import se.jsannemo.godel.harness.Stats;
import se.jsannemo.godel.harness.TimeLimitException;
import se.jsannemo.spooky.vm.SpookyVm;
import se.jsannemo.spooky.vm.StdLib;
import se.jsannemo.spooky.vm.VmException;
import se.jsannemo.spooky.vm.code.Executable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;

public final class Distributor {

  public static final int NO_MESSAGE_LIMIT = -1;
  private boolean suspended = false;
  private final Clocks clocks;
  private final Scheduler scheduler;
  private final int[] sources;
  private final ArrayList<SpookyVm> vms;
  private final ArrayList<ByteArrayOutputStream> standardOutput;
  private final int cycleLimit;
  private final int[] sent;

  private Distributor(
      ImmutableList<SpookyVm.Builder> builders,
      int cycleLimit,
      ArrayList<ByteArrayOutputStream> standardOutput,
      int messageLimit) {
    int nodes = builders.size();
    this.clocks = Clocks.create(nodes);
    this.scheduler = Scheduler.create(nodes);
    this.sources = new int[nodes];
    this.cycleLimit = cycleLimit;
    this.standardOutput = standardOutput;
    this.vms = new ArrayList<>();
    this.sent = new int[nodes];
    for (int i = 0; i < builders.size(); i++) {
      int name = i;
      this.sources[i] = -1;
      SpookyVm.Builder builder = builders.get(i);
      builder.addExtern("node", (vm) -> StdLib.setReturn(vm, 0, name));
      builder.addExtern("nodeCount", (vm) -> StdLib.setReturn(vm, 0, nodes));
      builder.addExtern("source", (vm) -> StdLib.setReturn(vm, 0, sources[name]));
      builder.addExtern(
          "send",
          (vm) -> {
            if (sent[name] == messageLimit) {
              throw new VmException("Exceeded message limit");
            }
            int target = StdLib.getArg(vm, 1);
            int msg = StdLib.getArg(vm, 0);
            if (target < 0 || target >= nodes) {
              throw new VmException("Message target out of bounds");
            }
            this.scheduler.send(new Scheduler.Message(this.clocks.time(name), name, target, msg));
          });
      builder.addExtern(
          "receive",
          (vm) -> {
            int source = StdLib.getArg(vm, 0);
            if (source < -1 || source >= nodes) {
              throw new VmException("Message source out of bounds");
            }
            if (!this.scheduler.receive(name, source)) {
              suspended = true;
            }
          });
      vms.add(builder.build());
    }
  }

  public Stats run() throws VmException {
    Stats stats = new Stats();
    while (true) {
      Optional<Integer> next = scheduler.schedule();
      if (next.isEmpty()) {
        break;
      }
      int which = next.get();
      SpookyVm vm = vms.get(which);
      if (this.scheduler.blockedFor(which) != Scheduler.NOT_WAITING) {
        receive(which);
      }
      while (!suspended) {
        stats = stats.max(new Stats(clocks.time(which), vm.getMaxMemory()));
        clocks.tick(which, 1);
        if (!vm.executeInstruction()) {
          break;
        }
        if (clocks.time(which) > this.cycleLimit) {
          throw new TimeLimitException();
        }
      }
      ByteArrayOutputStream outputStream = standardOutput.get(which);
      String output = outputStream.toString();
      outputStream.reset();
      if (!output.isEmpty()) {
        System.err.println("VM" + which + ": " + output);
      }
      suspended = false;
    }
    return stats;
  }

  private void receive(int name) throws VmException {
    int source = this.scheduler.blockedFor(name);
    Scheduler.Message m = this.scheduler.nextMessage(name, source);
    StdLib.setReturn(vms.get(name), 1, m.message);
    clocks.causality(name, m.sendTime);
  }

  String getOutput(int vm) {
    return standardOutput.get(vm).toString();
  }

  public static class Builder {
    private final ImmutableList<SpookyVm.Builder> builders;
    private final ArrayList<ByteArrayOutputStream> outputs = new ArrayList<>();
    private final int cycleLimit;
    private int messageLimit = -1;

    private Builder(Executable executable, int nodes, int cycleLimit) {
      ImmutableList.Builder<SpookyVm.Builder> buildersBuilder = ImmutableList.builder();
      for (int i = 0; i < nodes; i++) {
        buildersBuilder.add(SpookyVm.newBuilder(executable));
      }
      this.builders = buildersBuilder.build();
      this.cycleLimit = cycleLimit;
    }

    public Builder setMemorySize(int memoryCells) {
      for (int i = 0; i < builders.size(); i++) {
        builders.get(i).setMemorySize(memoryCells);
      }
      return this;
    }

    public Builder addExtern(String name, MultiExtern extern) {
      for (int i = 0; i < builders.size(); i++) {
        int finalI = i;
        builders.get(i).addExtern(name, (vm) -> extern.call(vm, finalI));
      }
      return this;
    }

    public Builder addStdLib(boolean nullOutput) {
      // Checks if standard library already added
      if (!outputs.isEmpty()) {
        return this;
      }
      for (int i = 0; i < builders.size(); i++) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        outputs.add(output);
        builders
            .get(i)
            .addStdLib()
            .setStdOut(
                nullOutput
                    ? new PrintStream(OutputStream.nullOutputStream())
                    : new PrintStream(output));
      }
      return this;
    }

    public Builder setMessageLimit(int messageLimit) {
      this.messageLimit = messageLimit;
      return this;
    }

    public Distributor.Builder addArray(String name, int[] input) {
      addExtern(name + "s", (vm, node) -> StdLib.setReturn(vm, 0, input.length));
      addExtern(
          name,
          (vm, node) -> {
            int i = StdLib.getArg(vm, 0);
            if (i < 0 || i >= input.length) {
              throw new VmException(node + " accessing out-of-bounds term " + i);
            }
            StdLib.setReturn(vm, 1, input[i]);
          });
      return this;
    }

    public Distributor build() {
      return new Distributor(builders, cycleLimit, outputs, messageLimit);
    }

    public Builder addOutput(String name, ArrayList<Integer> outputs) {
      addExtern(name, (vm, node) -> outputs.add(StdLib.getArg(vm, 0)));
      return this;
    }
  }

  public static Builder newBuilder(Executable executable, int nodes, int cycleLimit) {
    return new Builder(executable, nodes, cycleLimit);
  }

  @FunctionalInterface
  public interface MultiExtern {
    void call(SpookyVm vm, int node) throws VmException;
  }
}
