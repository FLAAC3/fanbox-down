package res

import ConfigManager
import ProgressManager
import modle.ProvideImgUrl
import modle.ProvidePathNameData
import modle.api.article.Article
import res.PathNameFormat.Companion.filterToFileName
import util.Message
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

/**
 * 管理文件存储路径
 * */
class FileManager (
    private val creatorId: String,
    creatorName: String
) {
    constructor(article: Article): this(article.creatorId, article.user.name)

    companion object {
        private val runPath: Path = Paths.get("""E:\FANBOX""") //设置运行目录（建议使用jar所在目录）
        val configPath: Path = runPath.resolve("config.txt") //配置文件路径（yaml格式）
        val cookiesPath: Path = runPath.resolve("cookies.txt") //存储 cookies 的文件路径
        val cookiesInitPath: Path = runPath.resolve("cookies.json") //初始化导入 cookies
        val cookiesSavePath: Path = runPath.resolve("cookies-save.json") //导出 cookies 的文件路径
        val progressInfoPath: Path = runPath.resolve("progress.txt") //记录每个作者的爬虫进度（yaml格式）
        //val htmlTempPath: Path = runPath.resolve("temp.html") //用于测试页面
        val outPath: Path = Paths.get(ConfigManager.config.outPath) //文件输出根目录

        init {
            if (outPath.notExists()) {
                Message.printlnErr1("输出目录", outPath, "不存在")
                throw Exception()
            }
            if (outPath.isRegularFile()) {
                Message.printlnErr1("输出目录", outPath, "不是文件夹")
                throw Exception()
            }
        }

        /**
         * 用于把文件保存到目录中
         * */
        fun saveImagesToPath (
            threadId: Int,
            path: Path,
            data: ProvideImgUrl
        ) {
            Files.createDirectories(path) //首先创建目录
            val firstName = path.parent.fileName //一级目录名
            val secondName = path.fileName //二级目录名
            val length = (data.freeSize + data.size).toString().length //文件名编号长度
            val b = ByteArray(1 * 1024 * 1024) // 1MB 缓冲池

            /**
             * 单个文件保存到本地的逻辑
             * */
            fun save (index: Int, suffix: String, inputStream: InputStream) {
                val outFile = path.resolve(index.toString().padStart(length, '0') + suffix).toFile()
                outFile.createNewFile() //创建文件
                val bis = BufferedInputStream(inputStream)
                val bos = BufferedOutputStream(FileOutputStream(outFile))
                try {
                    while (true) {
                        val readSize = bis.read(b)
                        if (readSize == -1) break
                        bos.write(b, 0, readSize)
                    }
                } finally {
                    bis.close()
                    bos.close()
                }
                Message.printlnInf1("[线程$threadId] 图片", "${firstName}\\${secondName}\\${outFile.name}", "保存成功")
            }

            repeat (data.freeSize) { i -> //先保存免费的（封面）
                val url = URI.create(data.nextFreeImgUrl())
                API.getImg(url) { suffix, inputStream ->
                    save(i + 1, suffix, inputStream)
                }
            }
            repeat (data.size) { i -> //再保存需要付费订阅的（文章图片）
                val url = URI.create(data.nextImgUrl())
                API.getImgWithCookies(url) { suffix, inputStream ->
                    save(i + 1 + data.freeSize, suffix, inputStream)
                }
            }
        }
    }

    private val pathNameFormat: PathNameFormat
    private var firstPath: Path //当前的一级目录

    init {
        val config = ConfigManager.config //程序配置
        val creatorConfig = config.creators[creatorId]
        pathNameFormat = if (creatorConfig == null)
            PathNameFormat(config.firstPath, config.secondPath, config.datePattern)
        else PathNameFormat(
            creatorConfig.firstPath ?: config.firstPath,
            creatorConfig.secondPath ?: config.secondPath,
            creatorConfig.datePattern ?: config.datePattern
        )

        var name = creatorName.filterToFileName()
        val postIdRangeSet = ProgressManager.getPostIdRangeSet(creatorId)
        if (postIdRangeSet != null) {
            val range = postIdRangeSet.first()
            if (range.isComplete())
                name = pathNameFormat.getFirstPathName(API.getArticle(range.endId!!))
        }
        firstPath = outPath.resolve(name)
    }

    /**
     * 获取二级目录（存放图片的目录）
     * */
    fun getFullPath (data: ProvidePathNameData): Path =
        firstPath.resolve(pathNameFormat.getSecondPathName(data))

    /**
     * 更新一级目录的名字
     * */
    fun renamePath (data: ProvidePathNameData) {
        val newPath = outPath.resolve(pathNameFormat.getFirstPathName(data))
        firstPath.toFile().renameTo(newPath.toFile())
        firstPath = newPath
    }

    /**
     * 获取目录名修改器
     * */
    fun getRenameManager (): RenameManager = RenameManager(creatorId, pathNameFormat, firstPath)
}