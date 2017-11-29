name := "try-dotty"

scalaVersion := "0.4.0-RC1"

resolvers ++= List(
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

libraryDependencies ++= List(
  "org.scalatest"     %        "scalatest_2.12" %     "3.0.4" % "test"
)

