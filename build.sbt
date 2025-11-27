ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

lazy val root = (project in file("."))
  .settings(
    name := "Wizard",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.apache.commons" % "commons-io" % "1.3.2"

    )
  )
coverageEnabled := true