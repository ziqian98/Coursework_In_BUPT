#include <stdlib.h>
#include <assert.h>
#include <stdio.h>

#include <unistd.h>
#include <arpa/inet.h>

#include "smart-read-write.h"

static int tcp_read_packet_length(int sockfd, void *buf, size_t length){
    int ret = 0;
    ssize_t bytes = 0;
    ssize_t bytes_left = length;
    do{
        bytes = read(sockfd, buf + length - bytes_left, bytes_left);
        if(bytes == -1){
            perror("tcp_read_packet_length-1");
            ret = -1;
            break;
        }else if(bytes == 0){
            /* EOF */
            perror("tcp_read_packet_length-2");
            ret = -1;
            break;
        }
        bytes_left -= bytes;
    }while(bytes_left > 0);

    return ret;
}

uint8_t *smart_read(int sockfd, uint16_t *size){
    uint16_t packet_header;
    if(tcp_read_packet_length(sockfd, &packet_header, 2) < 0){
        LOG("smart_read: packet header error");
        return NULL;
    }

    packet_header = ntohs(packet_header);

    *size = packet_header;

    uint8_t *bin_msg = (uint8_t *)malloc(sizeof(uint8_t) * packet_header);

    if(tcp_read_packet_length(sockfd, bin_msg, packet_header) < 0){
        LOG("smart_read: packet content error");
        free(bin_msg);
        return NULL;
    }
    return bin_msg;
}

int smart_write(int sockfd, message_t *msg){
    uint8_t *write_buffer = (uint8_t *)malloc(sizeof(uint8_t) * WRITE_BUF_SIZE);
    uint8_t *header_buffer = write_buffer;
    uint8_t *packet_buffer = write_buffer + 2;

    encode_message(msg, &packet_buffer);

    uint16_t msg_size = packet_buffer - (write_buffer + 2);

    put16bits(&header_buffer, msg_size);

    write(sockfd, write_buffer, msg_size + 2);

    free(write_buffer);

    return 0;
}

int udp_send_msg(int sockfd, struct sockaddr_in *server_addr, message_t *msg){

    uint8_t *udp_send_buffer = (uint8_t *)malloc(sizeof(uint8_t) * WRITE_BUF_SIZE);
    uint8_t *packet_buffer = udp_send_buffer;

    encode_message(msg, &packet_buffer);

    uint16_t nbytes = sendto(sockfd, udp_send_buffer, packet_buffer - udp_send_buffer,
                            0, (struct sockaddr *)server_addr, sizeof(*server_addr));

    assert(nbytes == packet_buffer - udp_send_buffer);

    free(udp_send_buffer);

    return 0;
}

uint8_t *udp_receive_msg(int sockfd){
    
}
