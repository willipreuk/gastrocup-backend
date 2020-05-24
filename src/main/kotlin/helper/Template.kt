package helper

import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroupFile

class Template(template: String) {
    private var st: ST

    init {
        val stGroup = STGroupFile(this.javaClass.classLoader.getResource("templates.stg"))
        st = stGroup.getInstanceOf(template)
    }

    fun getTemplate() = st
}