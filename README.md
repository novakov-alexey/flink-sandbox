A Flink application project using Scala and SBT.

To run and test your application locally, you can just execute `sbt run` then select the main class that contains the Flink job . 

You can also package the application into a fat jar with `sbt assembly`, then submit it as usual, with something like: 

```
flink run -c org.example.wordCount /path/to/your/project/my-app/target/scala-x.y.z/flink-sandbox-assembly-0.1.jar
```


You can also run your application from within IntelliJ:  select the classpath of the 'mainRunner' module in the run/debug configurations.
Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" dropbox. 

# Scala 3 Support

This project is using Ververica container image for Apache Flink 1.15, which depends on Scala 2.12. However, it does not have to have Scala 2.12 bundled, especially if you do not need to use Scala 2.12 specifically.
Below command builds Flink Image removing built-in Scala 2.12, which can be later used with [flink-scala-api](https://github.com/findify/flink-scala-api) library which support Scala 2.13 and 3.x.

```bash
make build-scala-image
```

# Ammonite 

There is helper predef script for Ammonite-REPL:

```bash
amm --predef scripts/flink-amm.sc
```

then use `table2` variable in Ammonite console:

```bash
table2.execute.print
```