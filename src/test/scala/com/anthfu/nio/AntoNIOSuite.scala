package com.anthfu.nio

import com.anthfu.nio.AntoNIO._
import munit.FunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.concurrent.ExecutionContext

class AntoNIOSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global

  test("Send a message to the server") {
    val message = Files.readString(Path.of(getClass.getClassLoader.getResource("message.txt").toURI))

    val host = "localhost"
    val port = 8080

    val server = serverChannel(host, port)
    assert(server.isOpen)

    clientChannel(host, port).flatMap { channel =>
      assert(channel.isOpen)
      write(channel, message.getBytes(StandardCharsets.UTF_8))
    }

    for {
      channel <- accept(server)
      bytes   <- read(channel)
    } yield {
      // Convert to string and discard NUL characters added by fixed-size buffer
      val res = new String(bytes, StandardCharsets.UTF_8).split("\u0000")(0)
      assertEquals(res, message)
    }
  }
}
