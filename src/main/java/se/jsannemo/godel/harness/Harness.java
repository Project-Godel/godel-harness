package se.jsannemo.godel.harness;

import se.jsannemo.spooky.vm.code.Executable;

import java.io.FileInputStream;

public interface Harness {

    void run(Executable executable, FileInputStream fileInputStream);

}
