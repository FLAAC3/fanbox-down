import org.apache.commons.io.FileUtils
import res.FileManager
import util.Message
import modle.config.Config
import java.nio.charset.StandardCharsets
import kotlin.io.path.name

object ConfigManager {
    val config: Config = FileUtils
        .readFileToString(FileManager.configPath.toFile(), StandardCharsets.UTF_8)
        .let(Config::of)

    init {
        //对全局目录名进行检查
        if (!config.firstPath.contains("\$creatorName")) {
            Message.printlnError("没有在全局一级目录中包含 \$creatorName 变量")
            throw Exception()
        }
        if (!config.secondPath.contains("\$title")) {
            Message.printlnError("没有在全局二级目录中包含 \$title 变量")
            throw Exception()
        }
        Message.printlnInfo("配置文件 ${FileManager.configPath.name} 已加载")
    }
}