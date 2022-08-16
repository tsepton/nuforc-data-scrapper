import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

@main def hello: Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()
  allLinksToPages.foreach(println)
}

def allLinksToPages(using browser: JsoupBrowser) = {
  val dataByDateDoc = browser.get("https://nuforc.org/webreports/ndxevent.html")
  (dataByDateDoc >> elementList("tbody tr td a") >> attr("href")).map(x => f"https://nuforc.org/webreports/${x}")
}
