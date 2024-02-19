package com.example

import java.text.NumberFormat
import java.util.Currency

case class Book(category: Category, author: Author, title: Title, price: Price)

case class Price(amount: BigDecimal, currency: Currency) {
  override def toString: String = {
    val numberFormat = NumberFormat.getCurrencyInstance
    numberFormat.setCurrency(currency)
    numberFormat.format(amount)
  }
}

case class Category(raw: String) extends AnyVal
case class Author(raw: String) extends AnyVal
case class Title(raw: String) extends AnyVal

case class Person(firstName: String, lastName: String)
