#2020CapstoneSpring

- Spring API server for Tattoo app


### 1. EC2 Setting
1.Install JDK 11 in EC2
```shell script
# aws coreetto 다운로드
sudo curl -L https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.rpm -o jdk11.rpm

# jdk11 설치
sudo yum localinstall jdk11.rpm

# jdk version 선택
sudo /usr/sbin/alternatives --config java

# java 버전 확인
java --version

# 다운받은 설치키트 제거
rm -rf jdk11.rpm
```

2.EC2 In-Bound Setting(Open XXXX(ex. 8080) port)

### 2. Nginx Setting (for Load Balancing)
1.Install Nginx
```shell script
sudo yum install nginx
## or
sudo amazon-linux-extras install nginx1
```
2.nginx.conf setting
```shell script
http{
  ##
  # It shoud be made in "http" or get it through include.
  ##
  
  sendfile on;
  tcp_nopush on;
  
  include /etc/nginx/conf.d/*.conf;
  include /etc/nginx/sites-enabled/ln.conf;
}
```

3.Create a new nginx.conf file in 'sites-available directory'.
```shell script
# file Name : lb.conf
upstream myserver{
  # <loadbalance type : default = round-robin>
  server 127.0.0.1:8081;
  server 127.0.0.1:8082;
}

server {
  listen 80;
  
  location /{
    proxy_pass http://myserver;
  }
}
```

4.Link .conf file.
```
sudo ln -s /etc/nginx/sites-available/lb.conf /etc/nginx/sites-enabled/
```

### 3. MySQL Setting
1.Install Mysql-Server
```shell script
#mysql-server 설치
sudo yum install mysql-server

# 초기 비밀번호 확인
sudo grep 'temporary password' /var/log/mysqld.log

# mysql 로그인
sudo mysql -u root -p

# 비밀 번호 변경 (Mysql 8.x)
alter user 'root'@'localhost' identified with mysql_native_password by 'NewPassword1!';

#변경사항 적용
flush privileges;
```

2.Allow remote access
```shell script
cd /etc/mysql/mysql.conf.d

# bind-address edit (Mysql 5.x만 Mysql 8.x 필요 없음)
sudo vim mysqld.cnf

# bind-address 수정
bind-address = 0.0.0.0 
```

3.Create DB & Table

4.load .csv files with Mysql loader

### 4.서버 자동 실행 (jar)
```shell script
while [ 1 ] 
	do 
		pid=`ps -ef | grep "myserver" | grep -v 'grep' | awk '{print $2}'` 
		if [ -z $pid ];then 
			if [ $count -lt 10 ];then 
				echo "server auto restart" 
				sudo nohub java -jar /home/ec2-user/myserver.jar &  
		fi 
		sleep 2 
	done
```