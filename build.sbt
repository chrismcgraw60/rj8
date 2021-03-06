import play.Project._

name := """reactive-java8-play"""

version := "1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaJpa,
  "org.jooq" % "jool" % "0.9.1",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.5.Final",
  "com.netflix.rxjava" % "rxjava-core" % "0.19.1",
  "com.google.inject" % "guice" % "3.0",
  "javax.inject" % "javax.inject" % "1"
  )

playJavaSettings

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}