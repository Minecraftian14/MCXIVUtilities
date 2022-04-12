## Args Utilities

Parse the arguments received in an application to a Map object.

#### importing in your project

```groovy
dependencies {
    implementation 'in.mcxiv.MCXIVUtilities:args:51d98e57ff:args'
}
```

### Creating a parser

```java
ArgsEvaler parser=new ArgsEvaler.ArgsEvalerBuilder()
        .build();
```

### Adding Indexed Arguments

These arguments are picked up according to their order of appearance.

If one defines the names as "a", "b" and "c"; then no matter what, the first three arguments received will be mapped to
first "a", then "b" and finally "c".

Input arguments: `Hello World !`
<br>
Referencing: `{a="Hello", b="World", c="!"}`

Input arguments: `! World Hello`
<br>
Referencing: `{a="!", b="World", c="Hello"}`

```java
ArgsEvaler parser=new ArgsEvaler.ArgsEvalerBuilder()
        .addIndexed("name_a")
        .addIndexed("name_b")
        .addIndexed("name_c")
        .build();
```

### Adding Named Arguments

To parse arguments which are equated with an `=` character and may occur in any order, use the named arguments.

If the names are defined as "a", "b" and "c":
<br>
Input arguments: `c=! a=World b=Hello`
<br>
Referencing: `{a="World", b="Hello", c="!"}`

```java
ArgsEvaler parser=new ArgsEvaler.ArgsEvalerBuilder()
        .addNamed("name_a")
        .addNamed("name_b")
        .addNamed("name_b")
        .build();
```

### Parsing Arguments and Retrieving Values

[//]: # (@formatter:off)
```java
// Get a HashMap
var map = parser.parse(args);
Object object = map.get("name_a");
String string = (String) map.get("name_a");

// Or get a Minimal Implementation of AbstractMap (which casts internally)
var map = parser.parseToResultMap(args);
int name = map.get("some_int_a");
```
[//]: # (@formatter:on)

### Specifying Data Types

To parse stuff directly to primitive types like `int`, we can specify it's class type.

```java
ArgsEvaler parser=new ArgsEvaler.ArgsEvalerBuilder()
        .addIndexed("num_a",long.class)
        .build();
```

#### Supported Types

```
// Primitive types, both boxed and unboxed. 
boolean.class
byte.class
char.class
short.class
int.class
float.class
long.class
double.class

// Other types
String.class
StringBuilder.class
StringBuffer.class

BigInteger.class
BigDecimal.class
AtomicInteger.class
AtomicLong.class
DoubleAdder.class
LongAdder.class

File.class
```

#### Adding custom types

Use the add resolver to add parsers for custom types.

```java
ArgsEvaler parser=new ArgsEvaler.ArgsEvalerBuilder()
        .addResolver(ByteBuffer.class,(c,s)->ByteBuffer.wrap(s.getBytes()))
        .build();
```
