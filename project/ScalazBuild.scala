import sbt._
import Keys._
import sbt.Package._
import java.util.jar.Attributes.Name._


object ScalazBuild extends Build {
  lazy val scalaz = Project("scalaz",
    file("."),
    settings = standardSettings
  ) aggregate (core, http, geo, example, scalacheckBinding, scalacheckGeo, tests, full)

  lazy val core = Project("scalaz-core",
    file("core"),
    settings = standardSettings ++ Seq(
      (sourceGenerators in Compile) <+= (sourceManaged in Compile) map {
        dir => Seq(Boilerplate.generateTupleW(dir))
      }
    )
  )

  lazy val geo = Project("scalaz-geo",
    file("geo"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq()
    )
  ) dependsOn (core)

  lazy val http = Project("scalaz-http",
    file("http"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.ServletApi)
    )
  ) dependsOn (core)

  lazy val scalacheckBinding = Project("scalaz-scalacheck-binding",
    file("scalacheck-binding"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.ScalaCheck)
    )
  ) dependsOn (core)

  lazy val scalacheckGeo = Project("scalaz-geo-scalacheck",
    file("geo-scalacheck"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq()
    )
  ) dependsOn (geo, scalacheckBinding)

  lazy val example = Project("scalaz-example",
    file("example"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.Specs, Dependency.ServletApi)
    )
  ) dependsOn (core, geo, http)

  lazy val tests = Project("scalaz-test-suite",
    file("tests"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.Specs)
    )
  ) dependsOn (core, geo, scalacheckBinding, scalacheckGeo)

  lazy val full = Project("scalaz-full",
    file("full"),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq()
    )
  ) dependsOn (core, scalacheckBinding, http, example, tests)

  object Dependency {
    val ServletApi = "javax.servlet" % "servlet-api" % "2.5"
    val ScalaCheck = "org.scala-tools.testing" % "scalacheck_2.8.1" % "1.8"
    val Specs = "org.scala-tools.testing" % "specs_2.8.1" % "1.6.7.2" % "test"
  }

  lazy val standardSettings = Defaults.defaultSettings ++ Seq(
    publishSetting,
    credentials += {
      // TODO first look up properties "build.publish.{user, password}" for CI build.
      Credentials(Path.userHome / ".ivy2" / ".credentials")
    },
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    packageOptions ++= Seq[PackageOption](ManifestAttributes(
      (IMPLEMENTATION_TITLE, "Scalaz"),
      (IMPLEMENTATION_URL, "http://code.google.com/p/scalaz"),
      (IMPLEMENTATION_VENDOR, "The Scalaz Project"),
      (SEALED, "true"))
    )
  )

  lazy val publishSetting = publishTo <<= (version) {
    version: String =>
      def repo(name: String) = name at "http://nexus-direct.scala-tools.org/content/repositories/" + name
      val isSnapshot = version.trim.endsWith("SNAPSHOT")
      val repoName = if(isSnapshot) "snapshots" else "releases"
      Some(repo(repoName))
  }
}

/* TODO Generate ScalaDoc and SXR Annotated Sources for all sources in the 'full' sub-project.
  lazy val allModules = Seq(core, http, geo, example, scalacheckBinding, scalacheckGeo, tests)

  class Full(info: ProjectInfo) extends ScalazDefaults(info) {
    lazy val packageFullAction = packageFull

    lazy val packageFull = {
      val allJars = Path.lazyPathFinder(Seq(core, geo, /*example,*/ http).map(_.outputPath)).## ** "*jar"
      val p = parentPath
      val extra = p("README") +++ p("etc").## ** "*"
      val sourceFiles = allJars +++ extra +++ (((outputPath ##) / "doc") ** "*")
      val packageName = "scalaz-full_" + buildScalaVersion + "-" + version.toString
      val copy = task {
        sbt.FileUtilities.copy(sourceFiles.get, outputPath / packageName, log)
        None
      }
      zipTask((outputPath ##) / packageName ** "*", outputPath / (packageName + ".zip") ) dependsOn (copy)
    } describedAs("Zip all artifacts")

    def deepSources = Path.finder { topologicalSort.flatMap { case p: ScalaPaths => p.mainSources.getFiles } }

    def allSourceRoots = topologicalSort.flatMap {case p: ScalaPaths => p.mainSourceRoots.getFiles.map(_.getAbsolutePath)}

    val sxr = "lib" / "sxr_2.8.0.RC2-0.2.4-SNAPSHOT.jar"

    override def documentOptions =
      SimpleDocOption("-Xplugin:" + sxr.asFile.getAbsolutePath) ::
      SimpleDocOption("-P:sxr:base-directory:" + allSourceRoots.mkString(":")) ::
      super.documentOptions

    lazy val fullDoc = scaladocTask("scalaz", deepSources, docPath, docClasspath, documentOptions)
  }
}

*/