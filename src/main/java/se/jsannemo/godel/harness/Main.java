package se.jsannemo.godel.harness;

import com.google.common.collect.ImmutableMap;
import se.jsannemo.godel.harness.task.DistributedAdd;
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
            .put("distributed-add", new DistributedAdd()).build();

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Missing input Spooky and test case");
      System.exit(1);
    }
    String sourcePath = args[0];
    File f = new File(sourcePath);
    String fileName = f.getName().split("\\.")[0];
    if (!TASKS.containsKey(fileName)) {
      System.err.println("No task with ID " + fileName + " found");
      System.exit(2);
    }
    String inputPath = args[1];
    try {
      byte[] binary = Compiler.compile(new FileInputStream(sourcePath));
      Executable exec = ExecutableParser.fromBinary(binary);
      TASKS.get(fileName).run(exec, new FileInputStream(inputPath));
    } catch (ParseException e) {
      System.err.println(e.currentToken.beginLine + ":" + e.currentToken.beginColumn + ": " + e.getMessage());
    } catch (ValidationException | FileNotFoundException e) {
      System.err.println(e.getMessage());
    } catch (InstructionException e) {
      System.err.println(e.getMessage());
    }
  }
}
