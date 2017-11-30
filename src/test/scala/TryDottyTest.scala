package org.k0ala.trydotty

import org.junit.{Test,Assert}
import junit.framework.TestCase


class TryDottySpec extends TestCase {

  @Test def testShouldNotThrow = {
    val tryDotty = TryDotty
    import tryDotty._
    println("Hi from TryDottySpec")
  }

  @Test def testShouldNotThrowEither = {
    val tryDotty = TryDotty
    import tryDotty._
    println("Hi from TryDottySpec (bis)")
  }

}


class TryDottySpec2 extends TestCase {

  @Test def testShouldNotThrow = {
    val tryDotty = TryDotty
    import tryDotty._
    println("Hi from TryDottySpec2")
  }

}
