@echo off
echo ������̨��...
start start.bat
timeout /nobreak /t 15
echo ��̨������ɣ�
echo չʾ����ַ��http://localhost:8888/api
start http://localhost:8888/api
echo �������ַ��http://localhost:8888/api/#/mana
start http://localhost:8888/api/#/mana
pause