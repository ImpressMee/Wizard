package de.htwg.wizard.di

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import de.htwg.wizard.control.GamePort
import de.htwg.wizard.control.controlComponent.component.GameComponent
import de.htwg.wizard.control.controlComponent.strategy.*
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.ModelComponent
import de.htwg.wizard.persistence.FileIO
import de.htwg.wizard.persistence.json.FileIOJson
import de.htwg.wizard.persistence.xml.FileIOXML

class StandardModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[FileIO].to[FileIOJson]   // oder FileIOJson
    bind[TrickStrategy].to[StandardTrickStrategy]
    bind[ModelInterface].to[ModelComponent]
    bind[GamePort].to[GameComponent]
  }
}
