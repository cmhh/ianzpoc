# Containerise the Application

Copy the jar from `../target/universal`, and then simply run:

```bash
docker built -t ianzpoc .
```

To run the application:

```bash
docker run -d --rm --name ianzpoc -p 9000:9000 ianzpoc
```
