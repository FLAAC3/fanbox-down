import modle.api.works.Item
import res.API
import res.Cache
import res.FileManager
import res.RenameManager
import util.Message

object RenameMode {
    /**
     * 改名模式，启动！！！
     * */
    fun start () {
        val creators = ConfigManager.config.creators
        for (creatorId in creators.keys) { //遍历配置文件中的 creatorId
            val renameManager = FileManager(creatorId, Cache.getCreatorName(creatorId)).getRenameManager()
            if (renameManager.needRename()) {
                if (renameManager.needRenameSecond()) {
                    val item = renameSecondPath(renameManager, creatorId)
                    if (renameManager.needRenameFirst())
                        item?.let { renameManager.renameFirstPath(it) }
                    renameManager.getFolderSet().forEach {
                        Message.printlnWar1("文件夹", "${renameManager.firstPathName}\\${it.name}", "无法识别，已跳过")
                    }
                } else renameFirstPath(renameManager, creatorId)
            }
            Message.printlnInf1("作者", Cache.getCreatorName(creatorId), "的作品目录改名完成")
        }
        Message.printlnInfo("爬虫程序运行结束")
    }

    /**
     * 更改二级目录，返回最后一次修改成功的 item
     * */
    private fun renameSecondPath (renameManager: RenameManager, creatorId: String): Item? {
        var successItem: Item? = null //记录每次修改成功的 item
        val urlArr = API.getWorkPages(creatorId).pageUrlArray
        main@for (i in urlArr.lastIndex downTo 0) {
            val items = API.getWorks(urlArr[i]).items
            for (j in items.lastIndex downTo 0) {
                val item = items[j]
                val result = renameManager.renameSecondPath(item)
                if (result) {
                    successItem = item
                    if (renameManager.isComplete()) break@main
                }
            }
        }
        return successItem
    }

    /**
     * 只更改一级目录时
     * */
    private fun renameFirstPath (renameManager: RenameManager, creatorId: String) {
        main@for (url in API.getWorkPages(creatorId).pageUrlArray) {
            for (item in API.getWorks(url).items) {
                if (renameManager.findSecondPath(item)) {
                    renameManager.renameFirstPath(item)
                    break@main
                }
            }
        }
    }
}