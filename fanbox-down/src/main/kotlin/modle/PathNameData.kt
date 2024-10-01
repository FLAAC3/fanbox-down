package modle

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * 创建目录所需要的全部数据（只有主构造函数的属性是，然后通过反射读取）
 * */
data class PathNameData (
    val title: String,
    val date: OffsetDateTime,
    val likeCount: Int,
    val commentCount: Int,
    val creatorName: String
): ProvidePathNameData {
    private val keyMap = HashMap<String, KProperty1<PathNameData, *>>().apply {
        val kClass = PathNameData::class
        val efficientKeys = kClass.primaryConstructor!! //从主构造函数中拿到参数的名字并转换成 HashSet
            .parameters.map { it.name!! }.toHashSet()
        for (pro in kClass.declaredMemberProperties) { //从属性中选择出 HashSet 中同样含有的，添加到 HashMap 中
            if (size == efficientKeys.size) break
            if (!efficientKeys.contains(pro.name)) continue
            this['$' + pro.name] = pro
        }
    }

    //所有可能的键长度（包含美元符）
    val keyLengthSet = HashSet<Int>().apply { keyMap.keys.forEach { add(it.length) } }

    /**
     * 根据键值查询数据（包含美元符）
     * */
    fun getKeyValue (key: String, datePattern: String): String? {
        val value = keyMap[key]?.get(this)
        return if (value is OffsetDateTime) {
            DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH).format(value)
        } else value?.toString()
    }

    override fun getPathData(): PathNameData = this
}
