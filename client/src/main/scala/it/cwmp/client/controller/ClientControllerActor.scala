package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
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
  case class RoomCreatePrivate(name: String, nPlayer: Int)
}

object ClientControllerActor {
  def apply(system: ActorSystem): ClientControllerActor = new ClientControllerActor(system)
}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @author Davide Borficchia
  */
class ClientControllerActor(system: ActorSystem) extends Actor{

  var roomViewActor: ActorRef = _

  override def preStart(): Unit = {
    super.preStart()
    // Initialize all actors
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController
    // TODO debug, remove before release
    roomViewActor ! RoomViewMessages.ShowGUI
  }

  def receive = roomManagerBehaviour
  //possibilità di aggiungere altri behavior
  //.orElse[Any, Unit](receiveAddItem)

  def becomeRoomsManager(): Unit = {
    context.become(roomManagerBehaviour)
  }

  /**
    * Questo metodo rappresenta il behavior da avere quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */
  def roomManagerBehaviour: Receive = {
    case ClientControllerMessages.RoomCreatePrivate(name, nPlayer) => println("prova", name, nPlayer) // TODO crea stanza
  }



}