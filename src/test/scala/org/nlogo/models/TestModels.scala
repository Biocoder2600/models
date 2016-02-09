package org.nlogo.models

import scala.collection.GenIterable
import scala.collection.GenSeq
import org.nlogo.api.Version
import org.scalatest.FunSuite
import Model.allModels
import Model.libraryModels
import scala.util.Try

trait TestModels extends FunSuite {

  def testModels(
    testName: String,
    includeTestModels: Boolean = false,
    includeOtherDimension: Boolean = false,
    filter: Model => Boolean = _ => true)(testFun: Model => GenIterable[Any]): Unit = {
    val models =
      (if (includeTestModels) allModels else libraryModels)
        .filter(includeOtherDimension || _.is3D == Version.is3D)
        .filter(filter)
    testModels(models, testName)(testFun)
  }

  def testModels(models: GenIterable[Model], testName: String)(testFun: Model => GenIterable[Any]): Unit =
    test(testName) {
      val allFailures: GenSeq[String] =
        (for {
          model <- models
          failures <- Try(testFun(model))
            .recover { case e => Seq(e.toString + "\n" + e.getStackTrace.mkString("\n")) }
            .toOption
          if failures.nonEmpty
          descriptions = failures.map(_.toString).filterNot(_.isEmpty).map("  " + _)
        } yield (model.quotedPath +: descriptions.toSeq).mkString("\n"))(collection.breakOut)

      if (allFailures.nonEmpty)
        fail((allFailures :+ s"(${allFailures.size} failing models)").mkString("\n"))
    }

  def testLines(
    section: Model => String, p: String => Boolean,
    msg: String => String = _ => "")(model: Model): Iterable[String] = {
    (for {
      (line, lineNumber) <- section(model).lines.zipWithIndex
      if p(line)
    } yield "  " + msg(line) + "line %4d |".format(lineNumber) + line).toIterable
  }

}
