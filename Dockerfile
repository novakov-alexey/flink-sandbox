FROM registry.ververica.com/v2.8/flink:1.15.2-stream3-scala_2.12-java11
RUN rm /flink/lib/flink-cep-scala_2.12-1.15.2-stream3.jar
RUN rm /flink/opt/flink-cep-scala_2.12-1.15.2-stream3.jar
RUN rm /flink/lib/flink-scala_2.12-1.15.2-stream3.jar
