package util

import org.flaac3.ColorfulPrinter.Model.Color
import org.flaac3.ColorfulPrinter.Model.TextColors
import org.flaac3.ColorfulPrinter.Printer
import org.flaac3.ColorfulPrinter.Template

object Message {
    private val messageInfo = Template {
        if (it == 0) TextColors(Color.GREEN, null)
        else if (it % 2 == 0) TextColors(Color(14), null)
        else TextColors.NO_COLOR
    }

    private val messageError = Template {
        if (it == 0) TextColors(Color.MAGENTA, null)
        else if (it % 2 == 0) TextColors(Color(14), null)
        else TextColors.NO_COLOR
    }

    /**
     * 打印一般信息
     * */
    fun printlnInfo (messages: String) {
        Printer().append(Color.GREEN, "Info: ")
            .append(null, messages)
            .println()
    }

    /**
     * 打印警告级别消息
     * */
    fun printlnWaring (messages: String) {
        Printer().append(Color.YELLOW, "Warning: ")
            .append(null, messages)
            .println()
    }

    /**
     * 打印错误级别消息
     * */
    fun printlnError (messages: String) {
        Printer().append(Color.MAGENTA, "Error: ")
            .append(null, messages)
            .println()
    }

    /**
     * 其它的消息打印模板 1
     * */
    fun printlnErr1 (m1: String, m2: Any, m3: String) {
        Printer(messageError).appendByTemplate("Error: ")
            .appendByTemplate(m1)
            .appendByTemplate(" “$m2” ")
            .appendByTemplate(m3)
            .println()
    }

    fun printlnInf1 (m1: String, m2: Any, m3: String) {
        Printer(messageInfo).appendByTemplate("Info: ")
            .appendByTemplate(m1)
            .appendByTemplate(" “$m2” ")
            .appendByTemplate(m3)
            .println()
    }

    fun printlnWar1 (m1: String, m2: Any, m3: String) {
        Printer {
            if (it == 0) TextColors(Color.YELLOW, null)
            else if (it % 2 == 0) TextColors(Color(14), null)
            else TextColors.NO_COLOR
        }.appendByTemplate("Warning: ")
            .appendByTemplate(m1)
            .appendByTemplate(" “$m2” ")
            .appendByTemplate(m3)
            .println()
    }

    /**
     * 其他的消息打印模板 2
     * */
    fun printlnInf2 (m1: String, m2: String, m3: String, m4: String) {
        Printer(messageInfo).appendByTemplate("Info: ")
            .appendByTemplate(m1)
            .appendByTemplate(" “$m2” ")
            .appendByTemplate(m3)
            .appendByTemplate(" “$m4” ")
            .println()
    }
}