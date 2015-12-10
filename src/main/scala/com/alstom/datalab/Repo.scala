package com.alstom.datalab

import java.sql.Date

import org.apache.spark.sql.{SQLContext, DataFrame}
import org.apache.spark.sql.functions._
import com.alstom.datalab.Util._


/**
  * Created by guillaumepinot on 05/11/2015.
  */

class Repo(RepoDir: String)(implicit val sqlContext: SQLContext) extends Serializable {
  import sqlContext.implicits._

  val repoDir = RepoDir
  val MDMRepository = RepoDir + "/MDM-ITC"
  val I_IDRepository = RepoDir + "/I-ID"
  val AIPServer = RepoDir + "/AIP-Server"
  val AIPSoftInstance = RepoDir + "/AIP-SoftInstance"
  val AIPApplication = RepoDir + "/AIP-Application"
  val AIP = RepoDir + "/AIP"

  def genAIP(currentDate:Date=null): Unit = {

    val aip_server = readAIPServer(currentDate)
    val aip_soft_instance = readAIPSoftInstance(currentDate)
    val aip_application = readAIPApplication(currentDate)

    val myConcat = new ConcatString("||")

    val dfres = aip_server
      .join(aip_soft_instance,
        aip_server("aip_server_hostname") === aip_soft_instance("aip_appinstance_hostname"), "left_outer")
      .join(aip_application,
        aip_soft_instance("aip_appinstance_shared_unique_id") === aip_application("aip_app_shared_unique_id"), "left_outer")
      .groupBy("aip_server_ip", "aip_server_adminby", "aip_server_function", "aip_server_subfunction")
      .agg(myConcat($"aip_appinstance_name").as("aip_app_name"),
        myConcat($"aip_appinstance_type").as("aip_appinstance_type"),
        myConcat($"aip_app_type").as("aip_app_type"),
        myConcat($"aip_app_state").as("aip_app_state"),
        myConcat($"aip_app_sensitive").as("aip_app_sensitive"),
        myConcat($"aip_app_criticality").as("aip_app_criticality"),
        myConcat($"aip_app_sector").as("aip_app_sector"),
        myConcat($"aip_appinstance_shared_unique_id").as("aip_app_shared_unique_id"))
      .filter(regexudf(ipinternalpattern)($"aip_server_ip"))
      .sort(desc("aip_app_name"), desc("aip_server_adminby"))
      .dropDuplicates(Array("aip_server_ip"))

    dfres.write.mode("overwrite").parquet(AIP)
  }

  def readAIP(): DataFrame = sqlContext.read.parquet(AIP)

  def readAIPServer(currentDate:Date=null): DataFrame = readRepo(AIPServer,currentDate)

  def readAIPSoftInstance(currentDate:Date=null): DataFrame = readRepo(AIPSoftInstance,currentDate)

  def readAIPApplication(currentDate:Date=null): DataFrame = readRepo(AIPApplication,currentDate)

  def readMDM(currentDate:Date=null): DataFrame = readRepo(MDMRepository,currentDate)

  def readI_ID(currentDate:Date=null): DataFrame = readRepo(I_IDRepository,currentDate)

  def readRepo(reponame: String, currentDate:Date): DataFrame = {

    val df = sqlContext.read.parquet(reponame)

    val maxDf = if (currentDate == null) {
      df.select(max(to_date($"filedate")).as("maxdate"))
    } else {
      df.filter(to_date($"filedate") <= currentDate).select(max(to_date($"filedate")).as("maxdate"))
    }

    df.join(maxDf, df("filedate") === maxDf("maxdate"), "inner").drop("maxdate")
  }
}

