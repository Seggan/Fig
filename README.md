# Fig
A functional, fractional-byte programming language. Tired of annoying Unicode codepages in your favorite golfing languages? Fig uses pure, printable ASCII.

## How to run
Simple. Download the release you want from the releases tab then run 
```shell
java -jar Fig-<version>.jar run <code file> [input]
```
If you want to format your code as a CGCC post, run
```shell
java -jar Fig-<version>.jar format <code file>
```
To print the lexed and parsed AST, run
```shell
java -jar Fig-<version>.jar debugRun <code file> [input]
```