package res

import modle.api.creator.User

object Cache {
    /**
     * key 是 creatorId，value 是 User 对象
     * */
    private val userMap = HashMap<String, User>()

    /**
     * 获取作者用户名
     * */
    @Synchronized
    fun getCreatorName (creatorId: String): String {
        var user = userMap[creatorId]
        if (user == null) {
            user = API.getCreator(creatorId).user //如果缓存中没有，则网络请求
            userMap[creatorId] = user
        }
        return user.name
    }
}