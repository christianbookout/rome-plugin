# sillylang
a stack-based interpreted hell language...

w/ java bindings included

## types
what is a language without a type system?

`i8` `i16` `i32` `i64` `f32` `f64` `u1`
`u16` `u16[]`

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

### java
- __class
- __method
- __execute
- __get_member
- __set_member

`(u16[] name) __class`

`(u16[]) (class) __method`

`(...) (method) __execute`