// ------------------------------------------------------------
// Global Settings
// ------------------------------------------------------------
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"

// ------------------------------------------------------------
// Test JVM Settings (WICHTIG für Coverage & GC)
// ------------------------------------------------------------
Test / fork := true

Test / javaOptions ++= Seq(
  "-Xms512M",
  "-Xmx2G",
  "-XX:+UseG1GC"
)

// ------------------------------------------------------------
// Coverage
// ------------------------------------------------------------
coverageEnabled := true

// ------------------------------------------------------------
// Dependencies
// ------------------------------------------------------------
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.14",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.scalafx" %% "scalafx" % "20.0.0-R31",
  "org.openjfx" % "javafx-base" % "20" classifier "win",
  "org.openjfx" % "javafx-controls" % "20" classifier "win",
  "org.openjfx" % "javafx-graphics" % "20" classifier "win",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "com.typesafe.play" %% "play-json" % "2.10.5"
)
Compile / mainClass := Some("de.htwg.wizard.Main")

// ------------------------------------------------------------
// Project
// ------------------------------------------------------------
lazy val root = (project in file("."))
  .settings(
    name := "Wizard"
  )
