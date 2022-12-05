// 这里就是kotlin 创建一个 DSL的例子，可见其非常强大

interface Element {
     fun render(builder: StringBuilder, indent: String)
 }

@DslMarker
annotation class HtmlTagMarker

 class TextElement(val text:String) : Element {
     override fun render(builder: StringBuilder, indent: String) {
         builder.append("$indent$text")
     }
 }

@HtmlTagMarker
 abstract class Tag(val name: String) : Element {
     val children = arrayListOf<Element>()
     val attributes = hashMapOf<String, String>()

     /**
      * 这个函数其实就是接收一个 tag 对象，并对其 init，然后返回 init 之后的对象。
      * <T:Element>规定了T 只能是一个 Element.
      * init: T.() -> Unit 表示初始化代码，这个初始化代码的操作对象就是一个 T对象。
      */
     protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
         tag.init()
         children.add(tag)
         return tag
     }

     override fun render(builder: StringBuilder, indent: String) {
         // 这里就体现了字符串模板的好处了
         builder.append("$indent<$name${renderAttributes()}>\n")
         for (c in children) {
             c.render(builder, indent + "  ")
         }
         builder.append("$indent</$name>\n")
     }

     private fun renderAttributes(): String {
         val builder = StringBuilder()
         for ((attr, value) in attributes) {
             builder.append(" $attr=\"$value\"")
         }
         return builder.toString()
     }

     override fun toString(): String {
         val builder = StringBuilder()
         render(builder, "")
         return builder.toString()
     }

 }

 // 带 Text 的 Tag，理应是 Tag子类
 abstract class TagWithText(name: String) : Tag(name) {

     /**
      * 这里为 String 类添加了一个+操作。
      * 相当于
      * this.+"blablabla"
      * 而 this 又可以省略，于是就有了
      * +"blablabla"
      *
      */
     operator fun String.unaryPlus() {
         children.add(TextElement(this))
     }
 }


 class Title : TagWithText("title")

 /**
  * 注意 Head 是如何包含 title 的
  */
 class Head : TagWithText("head") {
     /**
      * initTag(Title(), init) ：表示初始化一个 title 对象
      * 而对 title 进行初始化，当然需要的函数就是Title.() -> Unit
      */
     fun title(init: Title.() -> Unit) = initTag(Title(), init)
 }

 class HTML : TagWithText("html") {

     fun head(init: Head.() -> Unit) = initTag(Head(), init)
     fun body(init: Body.() -> Unit) = initTag(Body(), init)

 }

 // 创建 Html
 fun html(init: HTML.() -> Unit): HTML {
     val html = HTML()
     html.init()
     return html
 }


 class Body : BodyTag("body")
 class B : BodyTag("b")
 class P : BodyTag("p")
 class H1 : BodyTag("h1")

 class A : BodyTag("a") {
     var href: String
         get() = attributes["href"]!!
         set(value) {
             attributes["href"] = value
         }
 }

 /**
  * 这里有一个注意的点
  *
  * init: B.() -> Unit
  *
  * 在定义BodyTag时，并没有定义 B，而定义 B 时依赖 BodyTag 这里就有一个循环依赖，看来是没问题的。
  *
  */
 abstract class BodyTag(name: String) : TagWithText(name) {
     fun b(init: B.() -> Unit) = initTag(B(), init)
     fun p(init: P.() -> Unit) = initTag(P(), init)
     fun h1(init: H1.() -> Unit) = initTag(H1(), init)
     fun a(href: String, init: A.() -> Unit) {
         val a = initTag(A(), init)
         a.href = href
     }
 }


fun main(args: Array<String>) {

    val result = html {
        head {
            title {+"XML encoding with Kotlin"}
        }
        body {
            h1 {+"XML encoding with Kotlin"}
            p  {+"this format can be used as an alternative markup to XML"}

            // 一个具有属性和文本内容的元素
            a(href = "https://kotlinlang.org") {+"Kotlin"}

            // 混合的内容, 对 p进行初始化
            p {
                +"This is some"
                b {+"mixed"}
                +"text. For more see the"
                a(href = "https://kotlinlang.org") {+"Kotlin"}
                +"project"
            }
            p {+"some text"}

        }
    }

    println(result)
}