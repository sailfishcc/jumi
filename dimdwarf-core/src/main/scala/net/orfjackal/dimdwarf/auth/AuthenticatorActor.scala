package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.actors._

@ActorScoped
class AuthenticatorActor @Inject()(@Hub toHub: MessageSender[Any], checker: CredentialsChecker[Credentials]) extends Actor[AuthenticatorMessage] {
  def start() {}

  def process(message: AuthenticatorMessage) {
    message match {
      case IsUserAuthenticated(credentials) =>
        if (checker.isValid(credentials)) {
          toHub.send(YesUserIsAuthenticated(credentials))
        } else {
          toHub.send(NoUserIsNotAuthenticated(credentials))
        }
    }
  }
}
