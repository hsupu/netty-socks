# netty-socks5

A netty4-based socks5 repeater/server and shadowsocks repeater.

First, this is a netty example showing how to implement a socks5 proxy client/server. In this part, most is glue that combines socks5 protocol handler which netty has implemented.

Second, shadowsocks protocol is added to extend its usage scenario. And it also help us understanding the protocol.

## Features

- support socks5 protocol
- support shadowsocks protocol
- support IPv6
- support forwarding to another socks5 proxy

## TODO

- support SSL protocol
- support UDP
- optimize performance
