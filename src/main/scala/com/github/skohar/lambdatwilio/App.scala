package com.github.skohar.lambdatwilio

import java.nio.ByteBuffer
import java.util.Base64

import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.lambda.runtime.Context
import com.twilio.sdk.TwilioRestClient
import org.apache.commons.lang.exception.ExceptionUtils

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class App {

  val encryptedAccountSid = ""
  val encryptedAuthToken = ""
  val encryptedNumbers = ""
  val fromNumber = ""

  def handler(ipAddress: String, context: Context) {
    (for {
      accontSid <- Future(decrypt(encryptedAccountSid))
      authToken <- Future(decrypt(encryptedAuthToken))
      numbers <- Future(decrypt(encryptedNumbers).split(',').map(_.toInt))
    } yield {
      val client = new TwilioRestClient(accontSid, authToken)
      numbers.map { number =>
        val params = Map("From" -> fromNumber, "Url" -> "http://demo.twilio.com/docs/voice.xml", "To" -> s"+$number")
        val status = client.getAccount().getCallFactory.create(params).getStatus
        s"$number: $status"
      }.mkString(System.lineSeparator())
    }) onComplete {
      case Success(log) => println(log)
      case Failure(t) => System.err.println(ExceptionUtils.getStackTrace(t))
    }
  }

  def decrypt(encryptedText: String) = {
    val byteBufferEncryptedText = ByteBuffer.wrap(Base64.getDecoder.decode(encryptedText))
    val decryptRequest = new AWSKMSClient().decrypt(new DecryptRequest().withCiphertextBlob(byteBufferEncryptedText))
    new String(decryptRequest.getPlaintext.array())
  }
}
