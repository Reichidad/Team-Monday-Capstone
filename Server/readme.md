#2020CapstoneSpring

- Spring API server for Tattoo app

### Nginx Setting (for LoadBalancing)
1.nginx.conf setting
```shell script
http{
  ##
  # It shoud be made in "http" or get it through include.
  ##
  
  sendfile on;
  tcp_nopush on;
  
  include /etc/nginx/conf.d/*.conf;
  include /etc/nginx/sites-enabled/*;
}
```

2.Create a new nginx.conf file in 'sites-available directory'.
```shell script
# file Name : lb.cf
upstream myserver{
  # <loadbalance type : default = round-robin>
  server 192.168.0.1:4000;
  server 192.168.0.1:5000;
}

server {
  listen 80;
  
  location /{
    proxy_pass http://myserver;
  }
}
```

