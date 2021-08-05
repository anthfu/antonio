package com.anthfu.nio

import com.anthfu.nio.AntoNIO._
import munit.FunSuite

import scala.concurrent.ExecutionContext

class AntoNIOSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global

  test("Open a client channel to a server") {
    val server = serverChannel("localhost", 8080)
    assert(server.isOpen)

    clientChannel("localhost", 8080).map { channel =>
      assert(channel.isOpen)
      channel.close()
    }

    server.close()
  }
}
