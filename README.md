# 基本介绍
此程序使用 Kotlin 编写，用于批量下载 Pixiv Fanbox 的图片。目前支持的功能：

- 支持封面等图片的下载，图片自动编号，并且按照在文章出现的顺序来排序
- 支持自由设置一级和二级目录名，可以在配置文件中设置名字模板，可以用 $data 向名字中插入数据
- 支持设置下载起始点，比如在配置中指定文章ID或发布时间，程序会从这里开始爬虫
- 支持多线程并发下载，目前设置最多6个线程，如有需要可自行修改源代码
- 支持断点恢复和增量更新，程序正常退出会保存当前的进度，下次启动可以恢复
- 支持对已下载目录重新命名，可以在配置中指定新目录名模板
- 支持对每个作者单独配置，很多设置都分为全局和作者，可以满足不同需求
- 只在需要 cookie 的时候程序才会发送 cookie，否则就随机UA

# 配置文件
配置文件使用 Yaml 数据格式，方便使用和修改。先看完整的配置文件： 
```
#(一级目录必需，二级目录可选)$creatorName
#(一级目录可选，二级目录必需)$title
#(一级目录可选，二级目录可选)$date
#(一级目录不可，二级目录可选)$likeCount
#(一级目录不可，二级目录可选)$commentCount

outPath: E:\FANBOX
renameMode: false
datePattern: yyyy-MM-dd HH.mm.ss
ua: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36

firstPath: $creatorName - 最新作品：$title
secondPath: $date - $title [收藏$likeCount] [评论$commentCount]
#newFirstPath: $creatorName 更新日期：$date
#newSecondPath: $date - $title - 改个新名字

creators:
  kanibiimu:
    #postId: 666
    #publishedDate: 2024-07-01
    #datePattern: yyyy-MM-dd
    #firstPath: $creatorName
    #secondPath: $date - $title
    #newFirstPath: $creatorName 新的一级目录名
    #newSecondPath: $date - $title 作者局部配置
```
- renameMode：控制程序是下载模式还是改名模式
- datePattern：设置 $date 数据填充的时候的格式
- ua：设置请求头的UA标识，这个是和 Cookie 绑定的
- creators：值为一个Map数组，每个Map中key为作者的英文ID，值为这个作者的局部配置，局部配置优先于全局配置
- 所有用#注释的都是不必要的，用不到的话可以省略不写，datePattern也可省略，省略使用默认格式 yyyy-MM-dd

# 代码内路径配置
在 res.FileManager 类的伴生对象中有各个文件的路径，请自行修改：
![](/img/01.png)

# 导入 Cookies
目前程序没有用户名和密码登录的功能，需要自己在浏览器上拷贝包含 Cookies 的 cookies.json 数据文件到 res.FileManager 中定义的地址  
拷贝 Cookies 需要使用浏览器插件 **“Copy Cookies”** ，我在谷歌插件商店中找的，图标长这样：  
![](/img/02.png)  
然后就是有一个叫做 **“cf_clearance”** 的 cookie 字段拷贝不出来，需要使用浏览器开发者工具手动拷贝：  
首先运行程序导入 cookies.json，在提示缺少 cf_clearance 字段退出后，会生成一个 cookies.txt 文件，在这个文件里手动添加缺少的字段即可  

# 其它注意事项（重要）
- 二级目录推荐把 $date 数据放到目录名开头，因为按照程序目前的算法，只有这样才能使改名的速度最快
- 不要自行在二级目录的同级目录中放置其它文件夹，会导致改名的时候遍历作者所有作品去尝试匹配这个目录
- 不要自己修改目录名，程序是按照配置文件中的目录名模板去寻找文件的，改了之后很可能就找不着了
- 改名之后要记住把 new***Path 的值复制到对应的 ***Path，不然程序就找不着改名之后的文件了
- 如果当前是在配置中指定作品ID或者发布时间爬虫的，下次恢复爬虫进度必须删除这些信息，否则会覆盖已有文件重新爬一遍
