# sillylang
a stack-based interpreted hell language...

w/ java bindings included

## types
what is a language without a type system?

`i8` `i16` `i32` `i64` `f32` `f64` `u1`
`u16` `u16[]`

### java
of course, we require java compatibility.
`class` `method` `field` and `java` are all
provided for support :)

## literals

- numbers `i8.1` `i32.1000`
- strings `"hello"`
- `true`
- `false`

## built in functions

### general
- __duplicate
- __delete
- __swap

### math
- __add
- __subtract
- __multiply
- __divide
- __modulo

### java
- __class
- __method
- __execute
- __field
- __field_get
- __field_set

`(u16[] name) __class -> (class)`

`(class...) (i8|i16|i32|i64) (u16[]) (class) __method -> (method)`

`(u16[]) (class) __field -> (field)`

`(...) (any) (method) __execute -> (java)`

`(any) (field) __field_get -> (java)`