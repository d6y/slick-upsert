# Slick Upsert

This project contains an example of upserts using Slick 3.

# Running the code

There are three main methods supplied:

- `PkExample` - which is `insertOrUpdate` when you have an `O.PrimaryKey`
- `CompositePkExample` - an example with a composite primary key via `def pk = primaryKey...`
- `ManualExample` - when you want to do your own thing.

Run the one you want. E.g.,

```
$ sbt
sbt> runMain PkExample
```