package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.model.{ApiClientActor, ApiClientIncomingMessages}
import it.cwmp.client.view.AlertMessages
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ClientControllerMessages {

  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica per la gestione delle lobby delle stanze.
    * Quando lo ricevuto, viene mostrata all'utente l'interfaccia grafica.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int, token: String)
}

object ClientControllerActor {
  def apply(system: ActorSystem): ClientControllerActor = new ClientControllerActor(system)
}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @param system è l'[[ActorSystem]] che ospita gli attori che dovranno comunicare tra di loro
  *
  * @author Davide Borficchia
  */
class ClientControllerActor(system: ActorSystem) extends Actor {
  /**
    * Questo è l'attore che gestisce la view della lebboy delle stanze al quale invieremo i messaggi
    */
  var roomViewActor: ActorRef = _
  var roomApiClientActor: ActorRef = _

  /**
    * Questa metodo non va richiamato manualmente ma viene chiamato in automatico
    * quando viene creato l'attore [[ClientControllerActor]].
    * Il suo compito è quello di creare l'attore [[RoomViewActor]].
    * Una volta creato inizializza e mostra la GUI
    */
  override def preStart(): Unit = {
    super.preStart()
    // Initialize all actors
    roomApiClientActor = system.actorOf(Props[ApiClientActor], "roomAPIClient")
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController
    // TODO debug, remove before release
    roomViewActor ! RoomViewMessages.ShowGUI
  }

  /**
    * Questa metodo gestisce tutti i possibili behavior che può assumero l'attore [[ClientControllerActor]].
    * Un behavior è un subset di azioni che il controller può eseguire in un determianto momento .
    */
  override def receive: Receive = apiClientReceiverBehaviour orElse roomManagerBehaviour

  /**
    * Imposta il behavior del [[ClientControllerActor]] in modo da gestire solo la lobby delle stanze
    */
  def becomeRoomsManager(): Unit = {
    context.become(apiClientReceiverBehaviour orElse roomManagerBehaviour)
  }

  /**
    * Questo metodo rappresenta il behavior da avere quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */
  def roomManagerBehaviour: Receive = {
    case ClientControllerMessages.RoomCreatePrivate(name, nPlayer, token) =>
      roomApiClientActor ! ApiClientIncomingMessages.RoomCreatePrivate(name, nPlayer, token)
  }

  import it.cwmp.client.model.ApiClientOutgoingMessages._
  def apiClientReceiverBehaviour: Receive = {
    case RoomCreatePrivateSuccesful(token) =>
      roomViewActor ! AlertMessages.Info("Token", token)
    case RoomCreatePrivateFailure(reason) => AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
  }
}