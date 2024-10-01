package res

import modle.ProvidePathNameData

class PathNameFormat (
    private val firstPathFormat: String,
    private val secondPathFormat: String,
    private val datePattern: String
) {
    /**
     * 获取一级目录的名字
     * */
    fun getFirstPathName (data: ProvidePathNameData): String =
        createPathName(data, firstPathFormat, "\$likeCount", "\$commentCount")

    /**
     * 获取二级目录的名字
     * */
    fun getSecondPathName (data: ProvidePathNameData): String =
        createPathName(data, secondPathFormat)

    /**
     * 构建最终目录名通用函数
     * */
    fun createPathName (
        data: ProvidePathNameData, //构建目录名时额外还需要的数据
        pathFormat: String, //含有键的原始字符串
        vararg ignoreKeys: String //需要忽略的键
    ): String {
        val pathData = data.getPathData()
        val finalPathName = StringBuilder()
        fun recursion (start: Int) { //不好了，是递归！
            for (i in start ..< pathFormat.length) {
                if (pathFormat[i] == '$') {
                    main@for (length in pathData.keyLengthSet) {
                        val endIndex = i + length
                        if (endIndex > pathFormat.length) continue@main
                        val key = pathFormat.substring(i, endIndex)
                        for (ignoreKey in ignoreKeys) {
                            if (key == ignoreKey) continue@main
                        }
                        val value = pathData.getKeyValue(key, datePattern)
                        if (value == null) continue@main else {
                            finalPathName.append(value)
                            return recursion(endIndex)
                        }
                    }
                }
                finalPathName.append(pathFormat[i])
            }
        }
        recursion(0)
        return finalPathName.toString().filterToFileName()
    }

    /**
     * 获取二级目录的正则匹配表达式，主要是因为文章的收藏数和评论数会随着时间变化
     * */
    fun getSecondPathRegex (data: ProvidePathNameData): Regex {
        val pathName = createPathName(data, secondPathFormat, "\$likeCount", "\$commentCount")
        val regexStr = StringBuilder()
        for (c in pathName) when (c) {
            '.','+','^','$','(',')','[',']','{','}' -> regexStr.append('\\').append(c)
            else -> regexStr.append(c)
        }
        return Regex(
            regexStr.toString().replace(Regex("\\\$likeCount|\\\$commentCount"), "d+")
        )
    }

    companion object {
        /**
         * 把文件名不能含有的特殊字符替换掉
         * */
        fun String.filterToFileName (): String {
            var count = 0
            val out = StringBuilder()
            for (c in this) when (c) {
                '\\' -> out.append('╲')
                '/' -> out.append('／')
                '*' -> out.append('⁕')
                ':' -> out.append('：')
                '?' -> out.append('？')
                '<' -> out.append('＜')
                '>' -> out.append('＞')
                '|' -> out.append('│')
                '"' -> if (count++ % 2 == 0) out.append('“') else out.append('”')
                else -> out.append(c)
            }
            return out.toString()
        }
    }
}