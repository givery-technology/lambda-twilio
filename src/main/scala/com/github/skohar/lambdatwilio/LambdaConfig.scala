package com.github.skohar.lambdatwilio

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

case class LambdaConfig(accountSid: String, authToken: String, fromNumber: String, numbers: List[String],
                        slackWebHook: String)

object LambdaConfig {
  implicit val format = JsonNaming.snakecase(Json.format[LambdaConfig])
}
