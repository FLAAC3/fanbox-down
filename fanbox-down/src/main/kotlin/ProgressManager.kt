import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import modle.PostIdRange
import org.apache.commons.io.FileUtils
import res.FileManager
import util.Message
import java.nio.charset.StandardCharsets

object ProgressManager {
    private val progressInfoFile = FileManager.progressInfoPath.toFile()

    init {
        Message.printlnInfo("进度存储文件 ${progressInfoFile.name} 已加载")
    }

    /**
     * key 是 creatorId，value 是 PostIdRange 列表
     * LinkedHashSet<PostIdRange> 必须从 最旧到最新 排序
     * */
    private val progressMap: HashMap<String, LinkedHashSet<PostIdRange>> =
        if (progressInfoFile.exists()) {
            val content = FileUtils.readFileToString(progressInfoFile, StandardCharsets.UTF_8)
            if (content.trim() == "") HashMap()
            else Yaml.default.decodeFromString<HashMap<String, LinkedHashSet<PostIdRange>>>(content)
        } else { progressInfoFile.createNewFile(); HashMap() }

    /**
     * 根据进度文件查询作者的 PostIdRange 数组
     * */
    fun getPostIdRangeSet (creatorId: String): LinkedHashSet<PostIdRange>? = progressMap[creatorId]

    /**
     * 更新指定 creatorId 的 postIdRangeArr，没有就创建新的
     * LinkedHashSet<PostIdRange> 必须从 最旧到最新 排序
     * */
    @Synchronized
    fun update (creatorId: String, postIdRangeSet: LinkedHashSet<PostIdRange>) {
        val iterator = postIdRangeSet.iterator()
        while (iterator.hasNext()) { //移除已经爬虫完成的 PostIdRange，除了最后一个
            val postIdRange = iterator.next()
            if (postIdRange.isComplete() && iterator.hasNext()) {
                iterator.remove()
            }
        }
        progressMap[creatorId] = postIdRangeSet
        FileUtils.writeStringToFile(
            progressInfoFile,
            Yaml.default.encodeToString(progressMap),
            StandardCharsets.UTF_8
        )
        Message.printlnInfo("文件 ${progressInfoFile.name} 已更新")
    }
}