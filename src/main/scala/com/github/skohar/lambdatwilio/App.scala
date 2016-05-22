package com.github.skohar.lambdatwilio

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.twilio.sdk.TwilioRestClient
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.control.Exception._
import scalaz.\/

class App {

  def handler(event: SNSEvent, context: Context): String = (for {
    config <- App.description2config(context).leftMap(ExceptionUtils.getStackTrace)
  } yield try {
    val fs = config.numbers.map(number => Future {
      val client = new TwilioRestClient(config.accountSid, config.authToken)
      val params = Map("From" -> config.fromNumber, "Url" -> "http://demo.twilio.com/docs/voice.xml", "To" -> number)
      val status = client.getAccount().getCallFactory.create(params).getStatus
      s"$number: $status"
    })
    val f = Future.sequence(fs).map(_.mkString(System.lineSeparator()))
    Await.result(f, Duration.Inf)
  } catch {
    case t: Throwable =>
      val stackTraceString = ExceptionUtils.getStackTrace(t)
      context.getLogger.log(stackTraceString)
      new SlackApi(config.slackWebHook).call(new SlackMessage("lambda:catch", s"``` $stackTraceString ```"))
      stackTraceString
  }).merge
}

object App {

  def log(config: LambdaConfig, text: String) =
    new SlackApi(config.slackWebHook).call(new SlackMessage("lambda:debug", s"``` $text ```"))

  def description2config(context: Context) = \/.fromEither(allCatch either {
    val function = new AWSLambdaClient().getFunction(new GetFunctionRequest().withFunctionName(context.getFunctionName))
    val description = function.getConfiguration.getDescription
    Json.parse(description).as[LambdaConfig]
  })
}
