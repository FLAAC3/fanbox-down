package res

import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.Timeout
import util.Message
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Date

object ConnectionManager {
    private val defaultTimeOut = Timeout.ofSeconds(10) //默认超时时间
    private val connectionTimeToLive = Timeout.ofMinutes(1) //空闲连接存活时间

    init { //每隔10秒打印一次连接池信息
        Thread {
            while (true) {
                Message.printlnInfo(getConnectionInfo())
                Thread.sleep(10_000)
            }
        }.apply { isDaemon = true }.start()
    }

    /**
     * 默认的连接池
     * */
    private val connectionManager = PoolingHttpClientConnectionManager()
        .apply {
            defaultMaxPerRoute = 6 //单个域名调用上限
            maxTotal = 8 //最大连接数
        }

    /**
     * 默认的请求配置
     * */
    private val defaultRequestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(defaultTimeOut)
        .setResponseTimeout(defaultTimeOut)
        .build()

    /**
     * 从连接池获取 CloseableHttpClient
     * */
    fun getHttpClient (): CloseableHttpClient = HttpClients.custom()
            .setDefaultRequestConfig(defaultRequestConfig)
            .setConnectionManager(connectionManager)
            .evictIdleConnections(connectionTimeToLive)
            .build()

    /**
     * 连接池目前的情况
     * */
    fun getConnectionInfo (): String {
        val stats = connectionManager.totalStats
        return StringBuilder(SimpleDateFormat("网络连接池 [yyyy-MM-dd HH:mm:ss] ").format(Date()))
            .append("[进行中: ${stats.leased}; ")
            .append("等待: ${stats.pending}; ")
            .append("可用: ${stats.available}; ")
            .append("最大连接数: ${stats.max}]")
            .toString()
    }
}