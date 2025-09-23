## libcsv-java
Minimal implementation of CSV parser and formatter written in java for the following formats
- [RFC4180](https://datatracker.ietf.org/doc/html/rfc4180)

### Runtime requirements
- JRE 17+

### Example
```java
var parser = CsvParsers.ofStrictRfc4180(true);
var records =
        parser.parseString(
                """
                circle,"r=5,0"
                square,"d=2,0"
                """);
assertThat(records).containsExactly(List.of("circle", "r=5,0"), List.of("square", "d=2,0"));
```

```java
var parser = CsvParsers.ofStrictRfc4180(true);
var records = new ArrayList<List<String>>();
try (var anyReader = new StringReader(
        """
        a,b
        c,d
        """)) {
  var reader = parser.newRecordReader(anyReader);
  while (reader.hasMoreRecord()) {
    records.add(reader.readRecord());
  }
} catch (IOException e) { }

assertThat(records).containsExactly(List.of("a", "b"), List.of("c", "d"));
```