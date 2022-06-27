# Fig
A functional, fractional-byte programming language. Tired of annoying Unicode codepages in your favorite golfing languages? Fig uses pure, printable ASCII.

## How to run
Simple. Download the release you want from the releases tab then run 
```shell
java -jar Fig-<version>.jar execute <code file> [input]
```
That is only is your source file is formatted in Fig's codepage. If you wish to run a file using UTF-8, run
```shell
java -jar Fig-<version>.jar executeUTF8 <code file> [input]
```
If you want to format your code as a CGCC post, run
```shell
java -jar Fig-<version>.jar format <code file>
```
Note that the above command assumes the file is in UTF-8
