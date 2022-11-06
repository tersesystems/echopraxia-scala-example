package com.example

final case class Person(firstName: String, lastName: String, middleName: Option[String])
case class Bar(underlying: String) extends AnyVal
case class Foo(bar: Bar)