#include <stdio.h>
#include <stdint.h>
#include <assert.h>
#include <stdlib.h>

#include <pthread.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <signal.h>

#include "dns-server-lib.h"

#define RR_TYPE_NUM 32

// argv[1] -> conf
// argv[2] -> rr
// argv[3] -> cache

static pthread_t threads[2];

dns_server_t *dns_server = NULL;

static char *cache_file_name = NULL;

void stop(int signo){
    assert(dns_server != NULL);

    printf("stop name-server(%s)\n", dns_server->dns_server_desc);

    // wirte the cache datas into files
    assert(cache_file_name != NULL);
    FILE *fp = fopen(cache_file_name, "w");
    if(fp == NULL){
        perror("fopen");
        exit(0);
    }

    int i, j;
    node_t *cache_node;
    list_t *cache_list;
    resource_record_t *cache_rr;
    for(i = 0, j = 0; i < RR_TYPE_NUM; ++i){
        cache_list = dns_server->caches[i];
        if(list_size(cache_list) == 0){
            continue;
        }
        for(cache_node = cache_list->head; cache_node != NULL; cache_node = cache_node->next){
            cache_rr = (resource_record_t *)(cache_node->data);
            if(j != 0){
                fprintf(fp, "\n");
            }
            j++;
            if(cache_rr->type == A_RR_type){
                fprintf(fp, "%s, %u, IN, A, %u.%u.%u.%u",
                        cache_rr->name,
                        cache_rr->ttl,
                        cache_rr->rd_data.a_record.addr[0],
                        cache_rr->rd_data.a_record.addr[1],
                        cache_rr->rd_data.a_record.addr[2],
                        cache_rr->rd_data.a_record.addr[3]);

            }else if(cache_rr->type == NS_RR_type){
                fprintf(fp, "%s, %u, IN, NS, %s",
                        cache_rr->name,
                        cache_rr->ttl,
                        cache_rr->rd_data.ns_record.name);

            }else if(cache_rr->type == PTR_RR_type){
                fprintf(fp, "%s, %u, IN, PTR, %s",
                        cache_rr->name,
                        cache_rr->ttl,
                        cache_rr->rd_data.ptr_record.name);

            }else if(cache_rr->type == CNAME_RR_type){
                fprintf(fp, "%s, %u, IN, CNAME, %s",
                        cache_rr->name,
                        cache_rr->ttl,
                        cache_rr->rd_data.cname_record.name);

            }else if(cache_rr->type == MX_RR_type){
                fprintf(fp, "%s, %u, IN, MX, %s",
                        cache_rr->name,
                        cache_rr->ttl,
                        cache_rr->rd_data.mx_record.exchange);
            }
        }
    }

    fclose(fp);

    exit(0);
}

int main(int argc, char *argv[]){
    dns_server = new_dns_server(argv[1], argv[2], argv[3]);
	// argv[1] -> conf
// argv[2] -> rr
// argv[3] -> cache

    // store the cache file's name
    // this variable will be used in signal handler `stop`
    cache_file_name = argv[3];

    signal(SIGINT, stop);  //stop没有参数？
	

    print_dns_server(dns_server);

    pthread_create(&threads[0], NULL, tcp_server, (void *)dns_server);
    pthread_create(&threads[1], NULL, udp_server, (void *)dns_server);

    pthread_join(threads[0], NULL);
    pthread_join(threads[1], NULL);

    return 0;
}
