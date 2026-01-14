package de.htwg.wizard.di

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

import de.htwg.wizard.control.GamePort
import de.htwg.wizard.control.controlComponent.component.GameComponent
import de.htwg.wizard.control.controlComponent.strategy.*
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.ModelComponent

class StandardModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {

    bind[ModelInterface].to[ModelComponent]
    bind[TrickStrategy].to[StandardTrickStrategy]
    bind[GamePort].to[GameComponent]

  }
}
