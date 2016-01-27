name := """email-reporting"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.amazonaws" % "aws-java-sdk" % "1.10.49",
  "joda-time" % "joda-time" % "2.9.1",
  "com.gu" %% "play-googleauth" % "0.3.3",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesImport += "lib.TimeFilter"
// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

packageName in Universal := normalizedName.value

topLevelDirectory in Universal := Some(normalizedName.value)

riffRaffPackageType := (packageZipTarball in Universal).value

def env(key: String): Option[String] = Option(System.getenv(key))
riffRaffBuildIdentifier := env("TRAVIS_BUILD_NUMBER").getOrElse("DEV")
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
