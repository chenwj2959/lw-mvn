@echo off
::删除cache目录下所有文件
del /q /s D:\Demo\Java\Environment\apache-maven-3.3.9-test\bin\jar\*.*
::通过DIR获取cache目录下的所有子文件夹。然后删除获取到的文件夹 ">nul"不现实错误信息。
for /f "delims=" %%a in ('dir /ad/b/s D:\Demo\Java\Environment\apache-maven-3.3.9-test\bin\jar\*.*') do (rd /q /s "%%a")>nul


del /q /s D:\Demo\Java\Environment\apache-maven-3.3.9-test\bin\jar2\*.*
for /f "delims=" %%a in ('dir /ad/b/s D:\Demo\Java\Environment\apache-maven-3.3.9-test\bin\jar2\*.*') do (rd /q /s "%%a")>nul
echo end...
