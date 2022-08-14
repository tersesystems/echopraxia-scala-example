package com.example

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.plusscala.generic._
import com.tersesystems.echopraxia.plusscala.diff._

import java.time.Instant

trait AutoFieldBuilder extends FieldBuilder with AutoDerivation with DiffFieldBuilder
  with KeyValueCaseClassDerivation
  with OptionValueTypes with EitherValueTypes {
  implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)
}

object AutoFieldBuilder extends AutoFieldBuilder

object GenericMain {
  private val autoLogger = LoggerFactory.getLogger.withFieldBuilder(AutoFieldBuilder)

  def main(args: Array[String]): Unit = {

    AutoFieldBuilder.gen[Order]
    logOrder()
    //AutoFieldBuilder.gen[Option[Instant]]
    logOptionInstant()

    autoLogger.info("{}", _.value("int", 1))
    autoLogger.info("{}", _.keyValue("emptyArray", Seq.empty[Int]))
    autoLogger.info("{}", _.keyValue("some", Option(1)))
    autoLogger.info("{}", _.keyValue("none", None))
    autoLogger.info("{}", _.keyValue("right", Right(true)))
    autoLogger.info("{}", _.keyValue("left", Left(true)))
    //autoLogger.info("{}", _.keyValue("unit", ()))
    //autoLogger.info("{}", _.keyValue("future", Future.successful(1)))
  }

  private def logOptionInstant(): Unit = {
    val instant: Instant = Instant.now
    autoLogger.info("{}", _.keyValue("option", Some(instant)))
    autoLogger.info("{}", _.keyValue("instant", instant))
  }

  private def logOrder() = {
    val paymentInfo = PaymentInfo("41111111", Instant.now())
    val shippingInfo = ShippingInfo("address 1", "address 2")
    val sku1 = Sku(232313, "some furniture")
    val lineItems = Seq(LineItem(sku1, 1))
    val user = User("user1", 2342331)
    val order = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)
    autoLogger.info("{}", _.keyValue("order", order))

    autoLogger.info("diff {}", _.diff("diff", order, order.copy(owner = order.owner.copy(name = "user2"))))
  }
}

case class User(name: String, id: Int)

case class Sku(id: Int, description: String)

case class LineItem(sku: Sku, quantity: Int)

case class PaymentInfo(creditCardNumber: String, expirationDate: Instant)

case class ShippingInfo(address1: String, address2: String)

final case class Order(paymentInfo: PaymentInfo, shippingInfo: ShippingInfo, lineItems: Seq[LineItem], owner: User)
