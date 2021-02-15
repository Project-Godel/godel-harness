package se.jsannemo.godel.harness.taskutil;

import java.util.Scanner;

import static com.google.common.base.Preconditions.checkArgument;

public final class InputUtil {

  public static int[] readLenPrefixedInts(Scanner sc) {
    int n = sc.nextInt();
    int[] arr = new int[n];
    for (int i = 0; i < arr.length; i++) {
        arr[i] = sc.nextInt();
    }
    return arr;
  }

  public static int[] readBinaryString(Scanner sc) {
    String inputStr = sc.next();
    int[] input = new int[inputStr.length()];
    for (int i = 0; i < input.length; i++) {
      int c = inputStr.charAt(i) - '0';
      checkArgument(c == 0 || c == 1);
      input[i] = c;
    }
    return input;
  }
}
