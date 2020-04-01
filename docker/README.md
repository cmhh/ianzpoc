# Containerise the Application

As an experiment, there are two containers here: one with the standard Java runtime, and one with GraalVM instead.  To build either, copy the jar from `../target/universal` into the target folder, `jvm` or `graalvm`, and then simply run:

```bash
docker build -t ianzpoc:[jvm|graalvm] <target-folder>
```

To run the application:

```bash
docker run -d --rm --name ianz -p 9000:9000 ianzgraalvm
```
