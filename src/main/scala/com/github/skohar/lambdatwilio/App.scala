package com.github.skohar.lambdatwilio

import java.nio.ByteBuffer
import java.util.Base64

import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.twilio.sdk.TwilioRestClient

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class App {

  val encryptedAccountSid = "CiA24j/cGdUPG0yDaUxqUu1V4R19AY+jinRS1F/nmsh0ZhKpAQEBAgB4NuI/3BnVDxtMg2lMalLtVeEdfQGPo4p0UtRf55rIdGYAAACAMH4GCSqGSIb3DQEHBqBxMG8CAQAwagYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAwsa/96oLbfzcA82kACARCAPfm90xv05awO0QRVdaqQrLikgaeMUhGplijtmwGTMmlaMYpynLPoTPbKXQx8KWRnzK4qkkeR17uIEuS3lRQ="
  val encryptedAuthToken = "CiC1gS9AWLX+bVS9RKRqNkn13jMa04FRvFqtwdX/xgKPihKnAQEBAgB4tYEvQFi1/m1UvUSkajZJ9d4zGtOBUbxarcHV/8YCj4oAAAB+MHwGCSqGSIb3DQEHBqBvMG0CAQAwaAYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAzeDD4Zd9moAiuypyICARCAO1irA71k5TNWcw/yUACwakYPQz0BWR15pucj6f2qL1sj+OVgrN6EdkA0EQwqf5bShTS457OvsNf7se6a"
  val encryptedNumbers = "CiDANawXFaZJirHwhDlUwcf+TD4bTj2HAcDlvSqlXWhBIxKgAQEBAgB4wDWsFxWmSYqx8IQ5VMHH/kw+G049hwHA5b0qpV1oQSMAAAB3MHUGCSqGSIb3DQEHBqBoMGYCAQAwYQYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAybNaJS62OVpBOIAI0CARCANIJkkyGnwR1emMxsBYY5aVdl7gtMb/S0mS8IIAE7Z/6B4SVdvvyqd1vJsRMQYxFLxZKdBG4="
  val fromNumber = "+815031886131"

  def handler(event: SNSEvent, context: Context): String = call

  def call = {
    val f = for {
      accountSid <- Future(decrypt(encryptedAccountSid))
      authToken <- Future(decrypt(encryptedAuthToken))
      numbers <- Future(decrypt(encryptedNumbers).split(','))
    } yield {
      val fi = Future.traverse(numbers.toList) { number =>
        Future {
          val client = new TwilioRestClient(accountSid, authToken)
          val params = Map("From" -> fromNumber, "Url" -> "http://demo.twilio.com/docs/voice.xml", "To" -> s"+$number")
          val status = client.getAccount().getCallFactory.create(params).getStatus
          s"$number: $status"
        }
      }.map(_.mkString(System.lineSeparator()))
      Await.result(fi, Duration.Inf)
    }
    Await.result(f, Duration.Inf)
  }

  def decrypt(encryptedText: String) = {
    val byteBufferEncryptedText = ByteBuffer.wrap(Base64.getDecoder.decode(encryptedText))
    val decryptRequest = new AWSKMSClient().decrypt(new DecryptRequest().withCiphertextBlob(byteBufferEncryptedText))
    new String(decryptRequest.getPlaintext.array())
  }
}
