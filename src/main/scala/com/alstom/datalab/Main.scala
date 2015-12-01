package com.alstom.datalab

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by guillaumepinot on 10/11/2015.
  */

object Main {

  def main(args: Array[String]) {
    val usage = """
    Usage: DLMain [--D_REPO string] --method RepoProcessInFile|pipeline2to3|pipeline3to4|pipeline4to5 [methodsarg1 [methodarg2]]
                """
    if (args.length == 0) {
      println(usage)
      exit(1)
    }
    println("DLMain() : Begin")
    val arglist = args.toList
    type OptionMap = Map[Symbol, Any]

    var D_REPO="s3://alstomlezoomerus/DATA/Repository"

    var methodname = ""
    var methodarg1 = ""
    var methodarg2 = ""
    var methodarg3 = ""

    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      list match {
        case Nil => map
        case "--D_REPO" :: value :: tail =>
          D_REPO = value.toString
          nextOption(map ++ Map('D_REPO -> value.toString), tail)
        case "--method" :: value :: Nil =>
          methodname = value.toString
          map ++ Map('methodname -> methodname)
        case "--method" :: value :: arg1 :: Nil =>
          methodname = value.toString
          methodarg1 = arg1.toString
          map ++ Map('methodname -> methodname) ++ Map('methodarg1 -> methodarg1)
        case "--method" :: value :: arg1 :: arg2 :: Nil =>
          methodname = value.toString
          methodarg1 = arg1.toString
          methodarg2 = arg2.toString
          map ++ Map('methodname -> methodname) ++ Map('methodarg1 -> methodarg1) ++ Map('methodarg2 -> methodarg2)
        case "--method" :: value :: arg1 :: arg2 :: arg3 :: Nil =>
          methodname = value.toString
          methodarg1 = arg1.toString
          methodarg2 = arg2.toString
          methodarg3 = arg3.toString
          map ++ Map('methodname -> methodname) ++ Map('methodarg1 -> methodarg1) ++ Map('methodarg2 -> methodarg2) ++ Map('methodarg3 -> methodarg3)
        case option :: tail => println("Unknown option "+option)
          exit(1)
      }
    }
    val options = nextOption(Map(),arglist)
    println(options)

    val conf = new SparkConf()
      .setAppName("DataLab-"+methodname)
      .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    implicit val sc = new SparkContext(conf)
    implicit val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    sqlContext.setConf("spark.sql.shuffle.partitions", "10")

    val repo = new Repo(D_REPO)
    val pipe = new Pipeline(repo)

    methodname match {
      case "pipeline3to4" => if (methodarg3 != "") {
        pipe.pipeline3to4(methodarg1, methodarg2, methodarg3.toBoolean)
      } else {
        pipe.pipeline3to4(methodarg1, methodarg2)
      }
      case "pipeline2to3" => pipe.pipeline2to3(methodarg1, methodarg2)
      case "pipeline4to5" => pipe.pipeline4to5(methodarg1, methodarg2)
      case "RepoProcessInFile" => repo.ProcessInFile(methodarg1)
    }
  }
}
