package scaloi.syntax

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import scaloi.{Created, Gotten}

import scala.util.{Failure, Success}
import scalaz.std.string._
import scalaz.syntax.either._

class OptionOpsTest extends FlatSpec with OptionValues with Matchers {
  import OptionOps._

  behavior of "OptionOps"

  it should "tap options" in {
    var state = 0
    Some(1) <|? { s =>
      state = s
    } should equal(Some(1))
    state should equal(1)
    None <|? { s: Int =>
      state = 2
    } should equal(None)
    state should equal(1)
    Some(3).tap(state = _) should equal(Some(3))
    state should equal(3)
  }

  it should "tap empty options" in {
    var state = 0
    Some(1) -<| { state = 1 } should equal(Some(1))
    state should be(0)
    None -<| { state = 2 } should equal(None)
    state should be(2)
  }

  it should "flat left disjunct options" in {
    Some(1) <\/- 2.right should equal(1.left)
    Some(1) <\/- 2.left should equal(1.left)
    None <\/- 3.right should equal(3.right)
    None <\/- 4.left should equal(4.left)
  }

  it should "futurize options" in {
    val ex = new Exception

    Some(1).toFuture(ex).value.value should equal(Success(1))
    None.toFuture(ex).value.value should equal(Failure(ex))
  }

  it should "subtract options" in {
    Some(1) - 1 should equal(None)
    Some(1) - 2 should equal(Some(1))
    None - 3 should equal(None)
  }

  it should "optionally transform" in {
    Some((_: Int) * 2) transforming 5 should equal(10)
    None transforming "Bolivia" should equal("Bolivia")

    Some((s: String) => s"${s}inator") ~?> "option" should equal("optioninator")
    None ~?> "King Wiggle-Wut the Greater" should equal(
      "King Wiggle-Wut the Greater")
  }

  it should "filter empties" in {

    OptionNZ("") should equal(None)
    OptionNZ("a") should equal(Some("a"))
    "OptionNZ(0)" shouldNot compile // no int monoid in scope

    OptionNZ("A").orNZ("B") should equal(Some("A"))
    OptionNZ("").orNZ("B") should equal(Some("B"))
    OptionNZ("").orNZ("") should equal(None)

    Option("A").filterNZ should equal(Some("A"))
    Option("").filterNZ should equal(None)
  }

  it should "max things" in {
    import scalaz.std.anyVal.intInstance

    Option.empty[Int].max(None) should equal(None)
    None.max(Some(1)) should equal(Some(1))
    Some(2).max(None) should equal(Some(2))
    Some(2).max(Some(3)) should equal(Some(3))
    Some(4).max(Some(3)) should equal(Some(4))
    None.max(1) should equal(1)
    Some(1).max(2) should equal(2)
    Some(3).max(2) should equal(3)
  }

  it should "min things" in {
    import scalaz.std.anyVal.intInstance

    Option.empty[Int].min(None) should equal(None)
    None.min(Some(1)) should equal(Some(1))
    Some(2).min(None) should equal(Some(2))
    Some(2).min(Some(3)) should equal(Some(2))
    Some(4).min(Some(3)) should equal(Some(3))
    None.min(1) should equal(1)
    Some(1).min(2) should equal(1)
    Some(3).min(2) should equal(2)
  }

  it should "make get or creates" in {
    Some("gotten") orCreate { "created" } should equal(Gotten("gotten"))
    Option.empty[Int] orCreate { 12 } should equal(Created(12))

    var state = false
    Some('bip) orCreate { state = true; 'dip } should equal(Gotten('bip))
    state should be(false)
  }

}
