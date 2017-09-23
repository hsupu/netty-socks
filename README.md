# netty-socks

A netty4-based socks5 repeater/server and shadowsocks repeater.

First, this is a netty example showing how to implement a socks5 proxy client/server. In this part, most is glue that combines socks5 protocol handler which netty has implemented.

Second, shadowsocks protocol is added to extend its usage scenario. And it also help us understanding the protocol.

## Features

- support listening with socks5 protocol
- support accessing target directly
- support be proxied through socks5 protocol
- support be proxied through shadowsocks protocol
- support IPv6

## TODO

- support be proxied through SSL protocol
- support UDP
- optimize performance
- support PAC
