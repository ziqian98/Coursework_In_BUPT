command.o: command.c command.h
	gcc -Wall -c command.c

smart-tcp-read.o: smart-tcp-read.c smart-tcp-read.h
	gcc -Wall -c smart-tcp-read.c

dns.o: dns.c dns.h
	gcc -Wall -c dns.c

#list.o: list.c list.h
#	gcc -Wall -c list.c

dns-client-tcp.o: dns-client-tcp.c
	gcc -Wall -c dns-client-tcp.c

#dns-server-lib.o: dns-server-lib.c dns-server-lib.h
#	gcc -Wall -c dns-server-lib.c

dns-server-tcp.o: dns-server-tcp.c dns.h dns-server-lib.h
	gcc -Wall -c dns-server-tcp.c
    
#name-server.o: name-server.c
#	gcc -Wall -c name-server.c

obj: command.o smart-tcp-read.o dns.o dns-client-tcp.o dns-server-lib.o dns-server-tcp.o

server: name-server.o dns-server-lib.o dns-msg-lib.o list.o smart-read-write.o
	gcc name-server.o dns-server-lib.o dns-msg-lib.o list.o smart-read-write.o -lpthread -o server

client: resolver.o list.o dns-msg-lib.o smart-read-write.o
	gcc resolver.o list.o dns-msg-lib.o smart-read-write.o -o client

clean:
	rm -f *.o
	rm -f server client

run-com.root:
	sudo ./server ./conf/com.root.txt ./resource-record/com.root.txt ./cache/com.root.txt

run-root:
	sudo ./server ./conf/root.txt ./resource-record/root.txt ./cache/root.txt

run-cn.root:
	sudo ./server ./conf/cn.root.txt ./resource-record/cn.root.txt ./cache/cn.root.txt

run-edu.cn.root:
	sudo ./server ./conf/edu.cn.root.txt ./resource-record/edu.cn.root.txt ./cache/edu.cn.root.txt

run-local:
	sudo ./server ./conf/local.txt ./resource-record/local.txt ./cache/local.txt

run-org.root:
	sudo ./server ./conf/org.root.txt ./resource-record/org.root.txt ./cache/org.root.txt
