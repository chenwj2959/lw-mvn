# lw-mvn
个人轻量级maven仓库，解决在小型云服务器上搭建mavne私服后nexus占用大量内存的问题。

## 已完成
socket解析http协议
下载/缓存jar
网页浏览jar

## 计划方向
动态读取版本号
保存文件MD5值到文件
请求sha1本地生成
多remote配置
添加配置：请求sha1本地生成或远程请求、是否返回X-Checksum
支持SSL
上传jar命令
注册windows/linux服务脚本