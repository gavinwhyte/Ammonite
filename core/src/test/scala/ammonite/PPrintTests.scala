package ammonite

import utest._

import scala.collection.{immutable => imm}
import ammonite.Configs.default
case class Foo(integer: Int, sequence: Seq[String])
case class FooG[T](t: T, sequence: Seq[String])
object PPrintTests extends TestSuite{
  def check[T: PPrint](t: T, expected: String) = {
    val pprinted = PPrint(t)
    assert(pprinted == expected.trim)
  }

  val tests = TestSuite{
    'primitives{
      'Char{
        * - check('\n', "'\\n'")
        * - check('a', "'a'")
      }
      'Byte{
        * - check(123.toByte, "123")
        * - check(-123.toByte, "-123")
      }
      'Short{
        * - check(123.toShort, "123")
        * - check(-12345.toShort, "-12345")
      }
      'Int{
        * - check(123, "123")
        * - check(-1234567, "-1234567")
      }
      'Long{
        * - check(123456789012345L, "123456789012345L")
        * - check(-123456789012345L, "-123456789012345L")
      }
      'Float{
        * - check(0.75F, "0.75F")
        * - check(-13.5F, "-13.5F")
      }
      'Double {
        * - check(0.125, "0.125")
        * - check(-0.125, "-0.125")
      }
      'String{
        * - check("i am a cow", """ "i am a cow" """)
        * - check(""" "hello" """.trim, """ "\"hello\"" """)
        * - check("\n\n\n", """ "\n\n\n" """)
      }
      'Symbols{
        * - check('hello, """'hello""")
        * - check('I_AM_A_COW, """'I_AM_A_COW""")
       }
    }
    'misc{
      'Nothing - intercept[NotImplementedError](check(???, ""))
      'Null{
        check(null, "null")
        check(null: String, "null")
        check(Seq("look!", null: String, "hi"), """List("look!", null, "hi")""")
      }
      'Either{
        check(Left(123): Either[Int, Int], "Left(123)")
        check(Left(123): Left[Int, Int], "Left(123)")

        check(Left(123), "Left(123)")
        check(Right((1, "2", 3)), """Right((1, "2", 3))""")
      }
      'Options{
        check(Some(123), "Some(123)")
        check(None: Option[Int], "None")
        check(None: Option[Nothing], "None")
        check(None, "None")
        check(Some(None), "Some(None)")
      }
    }
    'collections{
      'Array - check(Array(1, 2, 3), "Array(1, 2, 3)")
      'Seq - check(Seq(1, 2, 3), "List(1, 2, 3)")
      'List - check(List("1", "2", "3"), """List("1", "2", "3")""")
      'Vector - check(Vector('omg, 'wtf, 'bbq), """Vector('omg, 'wtf, 'bbq)""")
      'Stream - check(Stream('omg, 'wtf, 'bbq), """Stream('omg, 'wtf, 'bbq)""")
      'Iterable - check(Iterable('omg, 'wtf, 'bbq), """List('omg, 'wtf, 'bbq)""")
      'Traversable - check(Traversable('omg, 'wtf, 'bbq), """List('omg, 'wtf, 'bbq)""")
      'Set - check(Set('omg), """Set('omg)""")
      'SortedSet- check(
        imm.SortedSet("1", "2", "3"),
        """TreeSet("1", "2", "3")"""
      )
      'Map{
        check(Map("key" -> "value"), """Map("key" -> "value")""")
      }

      'SortedMap - check(
        imm.SortedMap("key" -> "v", "key2" -> "v2"),
        """Map("key" -> "v", "key2" -> "v2")"""
      )
    }

    'tuples{

      check(Tuple1("123"), """Tuple1("123")""")
      check((1, 2, "123"), """(1, 2, "123")""")
      check(
        (1, 2, "123", (100L, 200L), 1.5F, 0.1),
        """(1, 2, "123", (100L, 200L), 1.5F, 0.1)"""
      )
    }
    'products{
      check(
        Foo(123, Seq("hello world", "moo")),
        """Foo(123, List("hello world", "moo"))"""
      )

      check(
        Seq(Foo(123, Seq("hello world", "moo"))),
        """List(Foo(123, List("hello world", "moo")))"""
      )
    }
    'Vertical{

      implicit def default = ammonite.PPrint.Config(25)
      'singleNested {
        * - check(
          List("12", "12", "12"),
          """List("12", "12", "12")"""
        )
        * - check(
          List("123", "123", "123"),
          """List("123", "123", "123")"""
        )
        * - check(
          List("1234", "123", "123"),
          """List(
            |  "1234",
            |  "123",
            |  "123"
            |)""".stripMargin
        )
        * - check(
          List("12345", "12345", "12345"),
          """List(
            |  "12345",
            |  "12345",
            |  "12345"
            |)""".stripMargin
        )
        * - check(
          Foo(123, Seq("hello world", "moo")),
          """Foo(
            |  123,
            |  List(
            |    "hello world",
            |    "moo"
            |  )
            |)""".stripMargin
        )
        * - check(
          Foo(123, Seq("moo")),
          """Foo(123, List("moo"))""".stripMargin
        )

      }
      'doubleNested{

        * - check(
          List(Seq("omg", "omg"), Seq("mgg", "mgg"), Seq("ggx", "ggx")),
          """List(
            |  List("omg", "omg"),
            |  List("mgg", "mgg"),
            |  List("ggx", "ggx")
            |)""".stripMargin
        )
        * - check(
          List(Seq("omg", "omg", "omg", "omg"), Seq("mgg", "mgg"), Seq("ggx", "ggx")),
          """List(
            |  List(
            |    "omg",
            |    "omg",
            |    "omg",
            |    "omg"
            |  ),
            |  List("mgg", "mgg"),
            |  List("ggx", "ggx")
            |)""".stripMargin
        )
        * - check(
          List(
            Seq(
              Seq("mgg", "mgg", "lols"),
              Seq("mgg", "mgg")
            ),
            Seq(
              Seq("ggx", "ggx"),
              Seq("ggx", "ggx", "wtfx")
            )
          ),
          """List(
            |  List(
            |    List(
            |      "mgg",
            |      "mgg",
            |      "lols"
            |    ),
            |    List("mgg", "mgg")
            |  ),
            |  List(
            |    List("ggx", "ggx"),
            |    List(
            |      "ggx",
            |      "ggx",
            |      "wtfx"
            |    )
            |  )
            |)""".stripMargin
        )
        * - check(
          FooG(Seq(FooG(Seq(Foo(123, Nil)), Nil)), Nil),
          """FooG(
            |  List(
            |    FooG(
            |      List(Foo(123, List())),
            |      List()
            |    )
            |  ),
            |  List()
            |)
          """.stripMargin
        )
        * - check(
          FooG(FooG(Seq(Foo(123, Nil)), Nil), Nil),
          """FooG(
            |  FooG(
            |    List(Foo(123, List())),
            |    List()
            |  ),
            |  List()
            |)""".stripMargin
        )
      }
    }

  }
}