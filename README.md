# Project Gödel Testing Harness
This tool can be used to test your solutions to [Project Gödel](https://godel.dev) problems.


## Usage
```
java --enable-preview -jar harness.jar problem-id.spooky inputfile
```

where `problem-id` is the problem ID of the Gödel problem you are solving.

Note that the problem requires Java 14 or later.

## Build
The tool uses Maven to build.

To build the tool, you may need to install the [Spooky VM](https://github.com/project-godel/spooky-vm) in your local Maven repository first.
Then, `mvn package` can be used
