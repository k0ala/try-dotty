
object TryDotty {
  def main(args: Array[String]) = {
    val xs: Set[Int] = Set.empty
    val x: Int = "hello".length
    println(xs + x)
  }
}


object IntersectionTypes {

  // cf. http://dotty.epfl.ch/docs/reference/intersection-types.html

  trait A {                                                                      
    def children: List[A]                                                        
  }                                                                              
  trait B {                                                                      
    def children: List[B]                                                        
  }                                                                              
  // when both types define the same member, the type must be a subtype of the intersection
  class C extends A with B {
    def children: List[A & B] = List()
  }
  val x: A & B = new C                                                           
  val ys: List[A & B] = x.children                                               
}

object ImplicitFunctionTypes {

  // cf. http://dotty.epfl.ch/docs/reference/implicit-function-types.html

  class Context(ci: Int) {
    def contextualInt: Int = ci
  }

  // implicit function type
  type Contextual[T] = implicit Context => T

  // gets applied to implicit arguments
  implicit val ctx: Context = new Context(4)

  def f(x: Int): Contextual[Int] = x * 3

  f(2) // is expanded to f(2)(ctx)

  // If the expected type of an expression E is an implicit function type,
  // and E is not already an implicit function value, then it is converted to one.

  def g(x: Contextual[Int]) = x * 5

  g(22)      // is expanded to g { implicit ctx => 22 }
  g(f(2))    // is expanded to g { implicit ctx => f(2)(ctx) }
  g(implicit ctx => f(22)(ctx)) // is left as it is

  // Using implicit function types to achieve the 'builder' pattern:

  object BuilderPattern {
    import collection.mutable.ArrayBuffer

    class Table {

    val rows = new ArrayBuffer[Row]
      def add(r: Row): Unit = rows += r
      override def toString = rows.mkString("Table(", ", ", ")")
    }

    class Row {
      val cells = new ArrayBuffer[Cell]
      def add(c: Cell): Unit = cells += c
      override def toString = cells.mkString("Row(", ", ", ")")
    }

    case class Cell(elem: String)

    def table(init: implicit Table => Unit) = {
      implicit val t = new Table
      init
      t
    }

    def row(init: implicit Row => Unit)(implicit t: Table) = {
      implicit val r = new Row
      init
      t.add(r)
    }

    def cell(str: String)(implicit r: Row) =
      r.add(new Cell(str))

    // now we can construct a table with the builder pattern:
    table {
      row {
        cell("top left")
        cell("top right")
      }
      row {
        cell("bottom left")
        cell("bottom right")
      }
    }

    // (expands to the following:)
    // table { implicit $t: Table =>
    //   row { implicit $r: Row =>
    //    cell("top left")($r)
    //    cell("top right")($r)
    //   }($t)
    //   row { implicit $r: Row =>
    //     cell("botttom left")($r)
    //     cell("bottom right")($r)
    //   }($t)
    // }

  }

}

object PhantomTypes {

  // cf. http://dotty.epfl.ch/docs/reference/phantom-types.html

  // defined in package scala:
  // trait Phantom { // only an `object` can extend this trait
  //   protected final type Any     // not a subtype of scala.Any
  //   protected final type Nothing // subtype of every subtype of this.Any
  //   protected final def assume: this.Nothing
  // }

  // Extending Phantom creates a separate type universe, but its types have no effect 
  // on runtime (exist only for // compile time safety). Can have multiple universes.

  object MyPhantoms extends Phantom {
    type Inky <: this.Any
    type Blinky <: this.Any
    type Pinky <: Inky
    type Clyde <: Pinky

    def pinky: Pinky = assume  // assume can be used to define values
    def clyde: Clyde = assume
  }

  object PhantomUser {
    import MyPhantoms._                                                            
    object MyApp {                                                                 
      def run(phantom: Inky) = println("run")                                      
      def hide(phantom: Blinky) = println("run")                                   
                                                                                  
      run(pinky)                                                                   
      run(clyde)                                                                   
    }                                                                              
  }

  object MyOtherPhantom extends Phantom {                                        
    type MyPhantom <: this.Any                                                   
    def myPhantom: MyPhantom = assume                                            
                                                                                
    def f1(a: Int, b: MyPhantom, c: Int): Int = a + c                            
                                                                                
    def f2 = {                                                                   
      f1(3, myPhantom, 2)                                                        
    }                                                                            
  }                                                                              




}

object LiteralSingletonTypes {

  // cf. http://dotty.epfl.ch/docs/reference/singleton-types.html

  // primitive literals can be used as types:
  val t: 42 = 42
  val x: "Jedi" = "Jedi"

  // functions can take or return singleton arguments:
  def f(t: Double): t.type = t
  val a: 1.2 = f(1.2)

}

object Enumerations {

  // cf. http://dotty.epfl.ch/docs/reference/enums/enums.html

  // An enumeration is used to define a type consisting of a set of named values.
  enum Color {
    case Red, Green, Blue
  }

  // equivalent
  enum class Color1
  object Color1 {
    case Red
    case Green
    case Blue
  }

  // An enum can also be parameterized -- parameter values are defined using `extends`:
  enum Color2(rgb: Int) {
    case Red   extends Color2(0xFF0000)
    case Green extends Color2(0x00FF00)
    case Blue  extends Color2(0x0000FF)
  }

  // methods of an enum: 
  val redTag: Int = Color.Red.enumTag;  assert(Color.enumValue(redTag) == Color.Red)
  val redColor: Color = Color.enumValueNamed("Red");   assert(redColor == Color.Red)
  val colors: collection.Iterable[Color] = Color.enumValues

  // custom definitons can be added to an enum:
  enum class Planet(mass: Double, radius: Double) {
    private final val G = 6.67300E-11
    def surfaceGravity = G * mass / (radius * radius)
    def surfaceWeight(otherMass: Double) =  otherMass * surfaceGravity
  }

  object Planet {
    case MERCURY extends Planet(3.303e+23, 2.4397e6)
    case VENUS   extends Planet(4.869e+24, 6.0518e6)
    case EARTH   extends Planet(5.976e+24, 6.37814e6)
    case MARS    extends Planet(6.421e+23, 3.3972e6)
    case JUPITER extends Planet(1.9e+27,   7.1492e7)
    case SATURN  extends Planet(5.688e+26, 6.0268e7)
    case URANUS  extends Planet(8.686e+25, 2.5559e7)
    case NEPTUNE extends Planet(1.024e+26, 2.4746e7)

    def main(args: Array[String]) = {
      val earthWeight = args(0).toDouble
      val mass = earthWeight/EARTH.surfaceGravity
      for (p <- enumValues)
        println(s"Your weight on $p is ${p.surfaceWeight(mass)}")
    }
  }
}

object AlgebraicDataTypes {

  // cf. http://dotty.epfl.ch/docs/reference/enums/adts.html

  // Here is an implementation of Option as an enum. Note that `extends` is omitted.
  enum Option1[+T] {
    case Some1[+T](x: T)
    case None1
  }

  // equivalent:
  enum Option2[+T] {
    case Some2[+T](x: T) extends Option2[T]
    case None2           extends Option2[Nothing] // covariant type args are minimized
  }
  // conversely, contravariant type args are maximized. If Option was invariant, would 
  // need to give the extends clause for None explicitly.

  // The type of values is that of the enum (inner cases can be created with `new`)
  val some: Option1[String]  = Option1.Some1("xyz")
  val none: Option1[Nothing] = Option1.None1

  // Just like other enums, ADTs can have methods on both class and companion object:
  enum class Option3[+T] {
    def isDefined: Boolean
  }
  object Option3 {
    def apply[T >: Null](x: T): Option3[T] =
      if (x == null) None3 else Some3(x)

    case Some3[+T](x: T) {
      def isDefined = true
    }
    case None3 {
      def isDefined = false
    }
  }

  // hybrid enum/ADT:
  enum Color(val rgb: Int) {                                                     
    case Red   extends Color(0xFF0000)                                           
    case Green extends Color(0x00FF00)                                           
    case Blue  extends Color(0x0000FF)                                           
    case Mix(mix: Int) extends Color(mix)                                        
  }                                                                              

}

object MultiversalEquality {

  // cf. http://dotty.epfl.ch/docs/reference/multiversal-equality.html

  sealed trait Meal
  sealed trait Pizza extends Meal
  case object Margherita extends Pizza
  sealed trait Pasta extends Meal
  case object Bolognese extends Pasta

  object WithoutSafety {
    val pizza: Pizza = ???
    pizza == Bolognese // can compare a Pizza to a Pasta (always false)
  }

  object WithSafety {
    import scala.language.strictEquality
    // Ensure a Pizza can only be compared to another Pizza:
    implicit def eqPizza: Eq[Pizza, Pizza] = Eq

    val pizza: Pizza = ???
    // does not compile:
    // pizza == Bolognese

    val meal: Meal = Bolognese
    pizza == meal // can still compare a Pizza to a Meal

    val any: Any = Bolognese 
    pizza == any // can even compare to Any
  }

  object Inline {

    object Config {
      inline val logging = false
    }

    object Logger {

      private var indent = 0

      inline def log[T](msg: => String)(op: => T): T =
        if (Config.logging) {
          println(s"${"  " * indent}start $msg")
          indent += 1
          val result = op
          indent -= 1
          println(s"${"  " * indent}$msg = $result")
          result
        }
        else op
    }
  }

}
