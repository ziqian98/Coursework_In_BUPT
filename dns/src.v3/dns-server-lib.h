#ifndef DNS_SERVER_LIB_H
#define DNS_SERVER_LIB_H

#include <stdint.h>
#include <stdbool.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#include "list.h"
#include "dns-msg-lib.h"

typedef struct dns_server dns_server_t;

// define dns server type
struct dns_server{
    char *dns_server_desc;
    char *ip_address;
    uint16_t port;

    list_t *SBELT;

    list_t **caches;
    
    bool recursion_avb;
    bool start_ns_flag;  

    int tcp_sockfd;
    int udp_sockfd;

    struct sockaddr_in sockaddr;

    list_t **rrs;

    // when udp server received a dns response packet,
    // it should notify the tcp server
    bool notify_flag;
    pthread_cond_t notify;  
    pthread_mutex_t lock;

    message_t *dns_response_msg;

    list_t *searched_ips;
};

dns_server_t *new_dns_server(const char *conf_file, const char *rr_file, const char *cache_file);

void destroy_dns_server(dns_server_t *dns_server);

void free_dns_server(dns_server_t *dns_server);

void *tcp_server(void *parm);

void *udp_server(void *parm);

void print_dns_server(dns_server_t *dns_server);


#endif
