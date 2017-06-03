package cats
package mtl

import mtl.instances.statet._
import mtl.instances.stateful._
import mtl.monad.Stateful

class StatefulUsage extends BaseSuite {

  test("set") {
    val _ =
      Stateful.set[StateTStrStateTInt, Int](1)
    val _1 =
      Stateful.set[StateTStrStateTInt, String]("err")
    val _2 =
      Stateful.setF[StateTStrStateTInt]("err")
  }

  test("get") {
    val _ =
      Stateful.get[StateTStrStateTInt, Int]
    val _1 =
      Stateful.get[StateTStrStateTInt, String]
  }



}
