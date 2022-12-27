A Flink application project using Scala and SBT.

To run and test your application locally, you can just execute `sbt run` then select the main class that contains the Flink job . 

You can also package the application into a fat jar with `sbt assembly`, then submit it as usual, with something like: 

```
flink run -c org.example.wordCount /path/to/your/project/my-app/target/scala-x.y.z/flink-sandbox-assembly-0.1.jar
```


You can also run your application from within IntelliJ:  select the classpath of the 'mainRunner' module in the run/debug configurations.
Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" dropbox. 


# Ammonite 

There is helper predef script for Ammonite-REPL:

```bash
amm --predef scripts/flink-amm.sc
```

then run in console:

```bash
table2.execute.print
```