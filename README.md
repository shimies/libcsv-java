## libcsv-java
Minimal implementation of CSV parsers and formatters written in java for the following formats
- [RFC4180](https://datatracker.ietf.org/doc/html/rfc4180)


### Runtime requirements
- JRE 17+


### Overview
The central APIs of parsing and formatting CSVs are `CsvParser` class and `CsvFormatter` class.
Parsers only unmarshal a CSV record into `List<String>` and formatters only marshal `List<String>` into a CSV record for now.
See the javadoc for full API documentation.


### Example
#### Parsing CSV
You can parse a CSV from a string using `CsvParser#parseString(String)` or a file using `CsvParser#parseString(Path, Charset)`:

```java
var parser = CsvParsers.ofStrictRfc4180(true);
var records =
        parser.parseString(
                """
                circle,"r=5,0"
                square,"d=2,0"
                """);

// assertThat(records).containsExactly(List.of("circle", "r=5,0"), List.of("square", "d=2,0"));
```

For large files or streaming, use `RecordReader` which allows you to write an interator-like way to access a record at a time.
`RecordReader` can be created by `CsvParser#newRecordReader(Reader)`.

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
}

// assertThat(records).containsExactly(List.of("a", "b"), List.of("c", "d"));
```

#### Formatting CSV
You can format records into a CSV from a string using `CsvFormatter#formatToString(List<String>)` or a file using `CsvFormatter#formatToFile(List<String>, Path, Charset)`:


```java
var formatter = CsvFormatters.ofRfc4180();
var records = List.of(List.of("1", "2"), List.of("Hello", "World"));
var sink = Files.createTempFile("test", ".csv");
formatter.formatToFile(records, sink, StandardCharsets.UTF_8);

// assertThat(sink).hasContent("1,2\r\nHello,World\r\n");
```

For large number of records or streaming, use `RecordWriter` which allows you to write an interator-like way to format a record at a time.
`RecordWriter` can be created by `CsvFormatter#newRecordWriter(Writer)`.


```java
var formatter = CsvFormatters.ofRfc4180();
var records = List.of(List.of("1", "2"), List.of("Hello", "World"));
var sink = Files.createTempFile("test", ".csv");
try (var bw = Files.newBufferedWriter(sink, StandardCharsets.UTF_8)) {
  var writer = formatter.newRecordWriter(bw);
  for (var record : records) {
    writer.writeRecord(record);
  }
}

// assertThat(sink).hasContent("1,2\r\nHello,World\r\n");
```


### References
For more details and API usage, see the javadoc in the source files and the test cases.

#### Parsers
- [CsvParsers](https://github.com/shimies/libcsv-java/blob/main/lib/src/main/java/io/github/shimies/csv/CsvParsers.java)
- [CsvParser](https://github.com/shimies/libcsv-java/blob/main/lib/src/main/java/io/github/shimies/csv/CsvParser.java)

#### Formatters
- [CsvFormatters](https://github.com/shimies/libcsv-java/blob/main/lib/src/main/java/io/github/shimies/csv/CsvFormatters.java)
- [CsvFormatter](https://github.com/shimies/libcsv-java/blob/main/lib/src/main/java/io/github/shimies/csv/CsvFormatter.java)
