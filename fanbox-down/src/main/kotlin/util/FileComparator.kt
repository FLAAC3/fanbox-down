package util

import filenamesort.FileNameComparator
import java.io.File

object FileComparator: Comparator<File> {
    private val nameComparator = FileNameComparator()

    /**
     * 按文件名升序排列
     * */
    override fun compare (o1: File?, o2: File?): Int {
        if (o1 == null || o2 == null) throw NullPointerException("文件排序不允许含有 null 值")
        return nameComparator.compare(o1.name, o2.name)
    }
}