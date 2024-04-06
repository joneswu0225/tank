@echo off
echo 启动后台中...
start start.bat
timeout /nobreak /t 15
echo 后台启动完成！
echo 展示端网址：http://localhost:8888/api
start http://localhost:8888/api
echo 管理端网址：http://localhost:8888/api/#/mana
start http://localhost:8888/api/#/mana
pause