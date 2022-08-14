package com.example

import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.plusscala.async.AsyncLoggerFactory
import com.tersesystems.echopraxia.plusscala.trace._
import com.tersesystems.echopraxia.plusscala.{Logger, LoggerFactory}

import java.util.UUID
import java.util.function.Consumer

object Main {
  import com.tersesystems.echopraxia.scripting._;

  val scriptString: String = """library echopraxia {
      |  function evaluate: (string level, dict ctx) ->
      |     level == "info";
      |}""".stripMargin
  val scriptCondition: Condition = ScriptCondition.create(false, scriptString: String, new Consumer[Throwable] {
    override def accept(t: Throwable): Unit = t.printStackTrace()
  }).asScala

  object MyFieldBuilder extends MyFieldBuilder

  trait MyFieldBuilder extends FieldBuilder {
    import com.tersesystems.echopraxia.api.Field

    implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)

    def uuid(name: String, uuid: UUID): Field = keyValue(name, uuid)
    def uuid(tuple: (String, UUID)): Field = keyValue(tuple)

    implicit val personToObjectValue: ToObjectValue[Person] = { person =>
      ToObjectValue(
        string("firstName", person.firstName),
        string("lastName", person.lastName)
      )
    }

    def person(name: String, person: Person): Field = keyValue(name, person)
    def person(tuple: (String, Person)): Field = keyValue(tuple)

    implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] =
      m => ToObjectValue(m.map(t => keyValue(t)))

    implicit def optionToValue[V: ToValue]: ToValue[Option[V]] = {
      case Some(v) => ToValue(v)
      case None => Value.nullValue()
    }
  }

  private val asyncLogger = AsyncLoggerFactory.getLogger.withFieldBuilder(MyFieldBuilder)
  private val logger = LoggerFactory.getLogger.withFieldBuilder(MyFieldBuilder)

  def main(args: Array[String]): Unit = {
    //    asyncLogger.withCondition(expensiveCondition).ifDebugEnabled { log =>
    //      val result = queryForExpensiveResult()
    //      log("async expensive result {}", fb => fb.number("result" -> result))
    //    }
    //
    //    Thread.sleep(2000L)

    //whatIfMultipleExceptions()

    logger.debug(scriptCondition, "logs at info level")


    def doStuff: Unit = {
      logger.info("{} {} {} {}", fb => fb.list(
        fb.number("number" -> 1),
        fb.bool("bool" -> true),
        fb.array("ints" -> Seq(1, 2, 3)),
        fb.string("strName" -> "bar")
      ))
    }

    doStuff
    conditionUsingFields()
    thisMethod()
    messAroundWithDeeplyNestedThings()
  }

  // use the field builder to build fields!
  private val willField: Field = MyFieldBuilder.person("person", Person("will", "sargent", None))
  private val condition: Condition = Condition { ctx =>
    val fields = ctx.fields
    val result = fields.contains(willField)
    result
  }

  val bigIntCondition = Condition(_.findNumber("$.bigInt").contains(BigInt("52")))

  def thisMethod(): Unit = {
    logger.info( "I log if the method is called thisMethod")
  }

  def conditionUsingFields() = {
    val thisPerson = Person("will", "sargent", None)
    logger.info(condition, "person matches! {}", _.keyValue("person" -> thisPerson))
  }

  def messAroundWithDeeplyNestedThings() = {
    val n1 = new RuntimeException("n1");
    val n2 = new RuntimeException("n2", n1);
    val n3 = new RuntimeException("n3", n2)
    val condition = Condition { ctx =>
      val value = ctx.findList("$.exception")
      val exception = value.head.asInstanceOf[Exception]
      exception.getMessage == "n1"
    }
    logger.info(condition, "derp {}", _.exception(n3))
  }

  //
  //  def queryForExpensiveResult(): Int = {
  //    Thread.sleep(500L)
  //    println("queryForExpensiveResult")
  //    1
  //  }
  //
  //  val expensiveCondition: Condition = Condition { (level: Level, context: LoggingContext) =>
  //    Thread.sleep(500L)
  //    println("test for expensive condition in " + Thread.currentThread())
  //    true
  //  }

  val isWill: Condition = Condition { (_: Level, context: LoggingContext) =>
    val list = context.findList("$.person[?(@.firstName == 'will')]")
    val map = list.head.asInstanceOf[Map[String, Any]]
    map("firstName") == "will"
    //context.findString("$.person.name").get == "will"
  }

  def whatIfMultipleExceptions(): Unit = {
    // only the last one is shown, others don't show up even though they have a name!
    // doesn't matter what the path is
    logger.error("Exception {} {}", fb => fb.list(
      fb.exception(new Exception("three")),
      fb.exception(new Exception("two")),
      fb.exception("one", new Exception("one"))
    ))
  }

  def doStuff(): Unit = {
    logger.info("Hello {}", _.string("herp" -> "derp"))
    logger.info("Hello {} {}", fb => {
      import fb._
      fb.list(
        number("foo" -> 1),
        obj("object", Seq(string("a" -> "b"), uuid("id", UUID.randomUUID())))
      )
    })
    logger.info("derp {}", fb => {
      import fb._
      fb.obj("obj", Map("foo" -> "bar"))
    })

    val uuids = Seq("first" -> UUID.randomUUID(), "second" -> UUID.randomUUID())
    logger.info("uuids = {}", fb => {
      fb.list(uuids.map(fb.uuid))
    })

    logger.info(isWill, "will is {}", _.person("person" -> Person("will", "sargent", None)))
  }
}


