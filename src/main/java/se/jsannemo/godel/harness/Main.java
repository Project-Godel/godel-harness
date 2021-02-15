package se.jsannemo.godel.harness;

import com.google.common.collect.ImmutableMap;
import se.jsannemo.godel.harness.task.BinarySort;
import se.jsannemo.godel.harness.task.DistributedAdd;
import se.jsannemo.godel.harness.task.Majority;
import se.jsannemo.spooky.compiler.Compiler;
import se.jsannemo.spooky.compiler.ParseException;
import se.jsannemo.spooky.compiler.ValidationException;
import se.jsannemo.spooky.vm.code.Executable;
import se.jsannemo.spooky.vm.code.ExecutableParser;
import se.jsannemo.spooky.vm.code.InstructionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {

  private static final ImmutableMap<String, Harness> TASKS =
      ImmutableMap.<String, Harness>builder()
          .put("distributed-add", new DistributedAdd())
          .put("majority", new Majority())
          .put("binary-sort", new BinarySort())
          .build();

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length != 2) {
      System.err.println("Missing input Spooky and test case");
      System.exit(2);
    }
    String sourcePath = args[0];
    File f = new File(sourcePath);
    String fileName = f.getName().split("\\.")[0];
    if (!TASKS.containsKey(fileName)) {
      System.err.println("No task with ID " + fileName + " found");
      System.exit(3);
    }
    Executable exec = null;
    try {
      exec = ExecutableParser.fromBinary(Compiler.compile(new FileInputStream(sourcePath)));
    } catch (ParseException e) {
      System.err.println(
          e.currentToken.beginLine + ":" + e.currentToken.beginColumn + ": " + e.getMessage());
      System.exit(4);
    } catch (ValidationException | InstructionException e) {
      System.err.println(e.getMessage());
      System.exit(4);
    }
    String inputPath = args[1];
    TASKS.get(fileName).runCase(exec, new FileInputStream(inputPath));
  }
}
