#ifndef SMART_READ_WRITE
#define SMART_READ_WRITE

#include <stdint.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "dns-msg-lib.h"

#define WRITE_BUF_SIZE 2048

uint8_t *smart_read(int sockfd, uint16_t *size);

int smart_write(int sockfd, message_t *msg);

int udp_send_msg(int sockfd, struct sockaddr_in *server_addr, message_t *msg);

uint8_t *udp_receive_msg(int sockfd);

#endif
