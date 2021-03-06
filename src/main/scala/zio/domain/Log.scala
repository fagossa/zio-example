package zio.domain

import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __}
import play.api.libs.functional.syntax._
import cats.syntax.all._

case class FileContent(fileName: String, contents: String)

case class LogWithName(fileName: String, log: Log)

case class User(friendlyName: String)
object User {
    implicit val reader: XmlReader[User] = attribute[String]("FriendlyName").map(apply)
}

case class To(user: User)
object To {
    implicit val reader: XmlReader[To] = (__ \ "User").read[User].map(apply)
}

case class From(user: User)
object From {
    implicit val reader: XmlReader[From] = (__ \ "User").read[User].map(apply)
}

case class Text(value: String)
object Text {
    implicit val reader: XmlReader[Text] = __.read[String].map(apply)
}

case class Message(from: From, to: To, text: Text)

object Message {
    def toLine(message: Message) =
        s"${message.from.user.friendlyName.replaceAll(",", ".")},${message.to.user.friendlyName.replaceAll(",", ".")},${message.text.value.replaceAll(",", ".")}"

    implicit val reader: XmlReader[Message] = (
        (__ \ "From").read[From],
        (__ \ "To").read[To],
        (__ \ "Text").read[Text]
    ).mapN(apply)
}

case class Log(messages: Seq[Message])
object Log {
    implicit val reader: XmlReader[Log] = (__ \ "Message").read(seq[Message]).default(Nil).map(apply _)

    def printContent(log: Log)(f: Message => Unit): Unit = {
        log.messages.foreach(f(_))
    }
}
