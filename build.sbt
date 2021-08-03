ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val antonio = (project in file("."))
  .settings(
    scalacOptions += "-target:11",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.27" % Test
    )
  )
