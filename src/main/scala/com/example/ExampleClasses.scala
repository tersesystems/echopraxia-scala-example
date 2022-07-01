package com.example

final case class IceCream(name: String, numCherries: Int, inCone: Boolean)
final case class Person(firstName: String, lastName: String, middleName: Option[String])
final case class EntityId(raw: Int) extends AnyVal
case class Bar(underlying: String) extends AnyVal
case class Foo(bar: Bar)