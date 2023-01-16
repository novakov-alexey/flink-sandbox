FROM flink:1.15.2
RUN rm $FLINK_HOME/opt/flink-cep-scala_2.12-1.15.2.jar
RUN rm $FLINK_HOME/lib/flink-scala_2.12-1.15.2.jar
RUN mkdir -p $FLINK_HOME/usrlib
COPY ./target/scala-3.1.2/flink-sandbox-*.jar $FLINK_HOME/usrlib/my-flink-job.jar