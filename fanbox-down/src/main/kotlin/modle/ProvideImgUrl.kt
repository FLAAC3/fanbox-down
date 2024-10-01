package modle

/**
 * 提供一次性的图片 Url 链接
 * */
interface ProvideImgUrl {
    val size: Int
    fun nextImgUrl (): String //需要 cookies 的图片和数量
    val freeSize: Int
    fun nextFreeImgUrl (): String //不需要 cookies 的图片和数量
}