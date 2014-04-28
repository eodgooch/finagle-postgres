package com.twitter.finagle.postgres.protocol

import org.specs2.specification.Example
import org.specs2.mutable.{ExpectationsBlock, ArgumentsArgs, FragmentsBuilder}

trait ConnectionSpec extends ExpectationsBlock {
  this: FragmentsBuilder with ArgumentsArgs =>

  args(sequential = true)

  implicit var connection = new ConnectionStateMachine()

  def withConnection[Unit](block: => Unit) {
    connection = new ConnectionStateMachine()
    block
  }

  implicit def inConnectionExample(s: String): InConnection = inConnection(new InExampleUnit(s))

  def inConnection(u: InExampleUnit): InConnection = new InConnection(u)
  class InConnection(underlying: InExampleUnit) {
    def inConnection(block: => Unit): Example = underlying.in(withConnection(block))
  }

  private[this] var result: Option[PgResponse] = None

  def send(msg: FrontendMessage)(implicit connection: ConnectionStateMachine) = {
    connection.onEvent(msg)
    result = None
  }

  def receive(msg: BackendMessage)(implicit connection: ConnectionStateMachine) = {
    result = connection.onEvent(msg)
  }

  def setState(state: State) {
    connection = new ConnectionStateMachine(state)
  }

  def response = result

}
