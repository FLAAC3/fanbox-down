package res

import modle.ProvidePathNameData
import util.FileComparator
import util.Message
import java.io.File
import java.nio.file.Path
import java.util.LinkedList
import kotlin.io.path.name

/**
 * 在 renameMode 开启的情况下，使用这个对象来重命名文件夹
 * */
class RenameManager (
    creatorId: String,
    private val pathNameFormat: PathNameFormat,
    private val firstPath: Path
) {
    var firstPathName = firstPath.name
    private val newFirstPathFormat: String?
    private val newSecondPathFormat: String?
    //列出一级目录下的所有文件夹，并且按文件名升序排序，也就是日期旧的下标为 0
    private val folderSet: LinkedHashSet<File>?

    init {
        val config = ConfigManager.config //程序配置
        val creatorConfig = config.creators[creatorId]
        if (creatorConfig == null) {
            newFirstPathFormat = config.newFirstPath
            newSecondPathFormat = config.newSecondPath
        } else {
            newFirstPathFormat = creatorConfig.newFirstPath ?: config.newFirstPath
            newSecondPathFormat = creatorConfig.newSecondPath ?: config.newSecondPath
        }
        val folders = firstPath.toFile().listFiles(File::isDirectory)
        folderSet = folders?.let { LinkedHashSet(it.sortedWith(FileComparator)) }
    }

    /**
     * 把当前的 folderSet 翻转成 linkedList
     * 这个就是文件名降序了，日期新的下标为 0
     * */
    private val reversalFolderList: LinkedList<File> by lazy {
        val out = LinkedList<File>()
        val iterator = folderSet!!.iterator()
        while (iterator.hasNext()) out.add(0, iterator.next())
        out
    }

    /**
     * 检查是否需要重命名（从总体上看）
     * */
    fun needRename (): Boolean = !(folderSet.isNullOrEmpty() || (newFirstPathFormat == null && newSecondPathFormat == null))

    /**
     * 是否需要修改一级目录
     * */
    fun needRenameFirst (): Boolean = newFirstPathFormat != null

    /**
     * 是否需要修改二级目录
     * */
    fun needRenameSecond (): Boolean = newSecondPathFormat != null

    /**
     * 检查是不是全部改名完成
     * */
    fun isComplete (): Boolean = folderSet!!.isEmpty()

    /**
     * 获取剩余的目录列表
     * */
    fun getFolderSet (): LinkedHashSet<File> = folderSet!!

    /**
     * 更改一级目录名，更改后不可以再调用 rename 系列方法
     * */
    fun renameFirstPath (data: ProvidePathNameData) {
        val newFile = getNewFirstFile(data)
        val newFileName = newFile.name
        firstPath.toFile().renameTo(newFile)
        Message.printlnInf2("目录", firstPathName, "改名为:", newFileName)
        firstPathName = newFileName
    }

    /**
     * 从目录名列表中筛选出完全匹配的，并且改名，成功后从列表中移除
     * */
    fun renameSecondPath (data: ProvidePathNameData): Boolean {
        val regex = pathNameFormat.getSecondPathRegex(data)
        val iterator = folderSet!!.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            val fileName = file.name
            if (regex.matches(fileName)) {
                val newFile = getNewSecondFile(data)
                file.renameTo(newFile)
                Message.printlnInf2("目录", "$firstPathName\\$fileName", "改名为:", "$firstPathName\\${newFile.name}")
                iterator.remove(); return true
            }
        }
        return false
    }

    /**
     * 倒序查找是否含有指定的目录名
     * */
    fun findSecondPath (data: ProvidePathNameData): Boolean {
        val regex = pathNameFormat.getSecondPathRegex(data)
        for (file in reversalFolderList) {
            if (regex.matches(file.name)) return true
        }
        return false
    }

    private fun getNewSecondFile (data: ProvidePathNameData): File =
        firstPath.resolve(
            pathNameFormat.createPathName(data, newSecondPathFormat!!)
        ).toFile()

    private fun getNewFirstFile (data: ProvidePathNameData): File =
        FileManager.outPath.resolve(
            pathNameFormat.createPathName(data, newFirstPathFormat!!, "\$likeCount", "\$commentCount")
        ).toFile()
}