#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "smart-read-write.h"
#include "dns-server-lib.h"
#include "dns-msg-lib.h"
#include "list.h"

#define LINE_BUF_SIZE 1024
#define PACKET_BUF_SIZE 2048

static const int RR_TYPE_NUM = 32;
static const int RR_LIST_SIZE = 5;

static int RR_LIST[] = {1, 2, 5, 12, 15};

uint8_t packet_buffer[PACKET_BUF_SIZE];

static int configure_dns_server(dns_server_t *dns_server,
                                const char *conf_file);

static int read_resource_record(list_t ***rrs,
                                const char *file);

static list_t *search_resouce_record(message_t *query_msg,
                                     list_t **caches,
                                     bool *all_hit,
                                     list_t *question_status);

static int ask_other_name_server(message_t *query_msg,
                                 question_t *q,
                                 dns_server_t *dns_server);

static int build_response_msg(message_t *query_msg,
                              list_t *ans_list,
                              list_t *aut_list,
                              list_t *add_list);

static int add_msg_rr_into_list(list_t *list,
                                resource_record_t *rr);

static list_t *find_all_ns_ips(list_t *aut_list,
                               list_t *add_list);

static bool child_domain(const char *parent,
                         const char *child);

static void print_msg_site_info(dns_server_t *dns_server,
                                struct sockaddr_in *server_addr);

dns_server_t *new_dns_server(const char *conf_file, const char *rr_file, const char *cache_file){
    dns_server_t *dns_server = (dns_server_t *)malloc(sizeof(dns_server_t));

    configure_dns_server(dns_server, conf_file);
    
    read_resource_record(&dns_server->rrs, rr_file);

    read_resource_record(&dns_server->caches, cache_file);

    return dns_server;
}

static list_t *check_mx_answers(list_t **rrs, list_t *ans_list){
    node_t *ans_node, *a_node;

    resource_record_t *ans_rr, *a_rr;

    list_t *add_list = new_list();

    for(ans_node = ans_list->head; ans_node != NULL; ans_node = ans_node->next){
        ans_rr = (resource_record_t *)(ans_node->data);
        if(ans_rr->type != MX_RR_type){
            continue;
        }
        for(a_node = rrs[A_RR_type]->head; a_node != NULL; a_node = a_node->next){
            a_rr = (resource_record_t *)(a_node->data);
            if(strcmp(ans_rr->rd_data.mx_record.exchange, a_rr->name) == 0){
                list_add_end(add_list, (void *)copy_resource_record(a_rr));
            }
        }
    }
    return add_list;
}


static void insert_and_update_cache(message_t *recv_msg, dns_server_t *dns_server){
    time_t now = time(0);

    resource_record_t *ans_rr, *cache_rr, *copy_rr;
    node_t *cache_node;
    list_t *new_rrs = new_list();

    // Step 1, extract resource record from `recv_msg`
    bool rr_exist;
    if(recv_msg->answers != NULL && recv_msg->ansCount != 0){
        for(ans_rr = recv_msg->answers; ans_rr != NULL; ans_rr = ans_rr->next){
            rr_exist = false;
            for(cache_node = dns_server->caches[ans_rr->type]->head;
                cache_node != NULL;
                cache_node = cache_node->next){

                cache_rr = (resource_record_t *)(cache_node->data);
                if(resource_record_cmp(ans_rr, cache_rr)){
                    // add this resource_record into cache
                    rr_exist = true;
                    break;
                }
            }
            if(!rr_exist){
                copy_rr = (void *)copy_resource_record(ans_rr);
                copy_rr->ttl += (uint32_t)now;
                list_add_end(new_rrs, copy_rr);
            }
        }
    }

    list_t *tmp_list = new_list();
    list_t *tmp_cache_list = NULL;

    // using TTL update resource record in caches
    resource_record_t *del_rr;
    int i;
    for(i = 0; i < RR_LIST_SIZE; ++i){
        tmp_cache_list = dns_server->caches[RR_LIST[i]];
        while(!list_is_empty(tmp_cache_list)){
            cache_rr = (resource_record_t *)(list_remove_start(tmp_cache_list));
            if(cache_rr->ttl < (uint32_t)now){
                free_resource_record(cache_rr);
            }else{
                list_add_end(tmp_list, (void *)cache_rr);
            }
        }
        while(!list_is_empty(tmp_list)){
            list_add_end(tmp_cache_list, list_remove_start(tmp_list));
        }
    }

    // insert the new resource record into caches
    while(!list_is_empty(new_rrs)){
        ans_rr = (resource_record_t *)(list_remove_start(new_rrs));
        list_add_end(dns_server->caches[ans_rr->type], (void *)ans_rr);
    }

    free(tmp_list);
    free_list(new_rrs);
    return;
}

void *udp_server(void *parm){
    printf("Start up udp server ...\n");
    dns_server_t *dns_server = (dns_server_t *)parm;
    
    struct sockaddr_in client_addr;
    socklen_t client_addr_size = sizeof(client_addr);

    if(bind(dns_server->udp_sockfd, (struct sockaddr *)&(dns_server->sockaddr), sizeof(dns_server->sockaddr)) < 0){
        perror("udp bind");
        return NULL;
    }

    uint16_t packet_size;
    struct sockaddr_in query_server;

    list_t *ns_ips = new_list();
    node_t *ip_node;

    for(ip_node = dns_server->SBELT->head;
            ip_node != NULL;
            ip_node = ip_node->next){

        list_add_end(dns_server->searched_ips, (void *)(uint64_t)inet_addr((char *)(ip_node->data)));
    }

    struct sockaddr_in ns_server;

    for(;;){
        packet_size = recvfrom(dns_server->udp_sockfd, packet_buffer, PACKET_BUF_SIZE,
                                   0, (struct sockaddr *)&client_addr, &client_addr_size);
        perror("recvfrom:");
        // decode the binary packet
        message_t *recv_msg = (message_t *)calloc(1, sizeof(message_t));

        decode_message(recv_msg, packet_buffer, packet_size);

        // If the received message have answers, then insert answers into caches.
        // Update the cache before function return.
        insert_and_update_cache(recv_msg, dns_server);

        // print for debug
        printf("UDP SERVER RECEIVED A PACKCET:\n");
        print_query(recv_msg);

        if(recv_msg->qr == 0){
            memcpy(&query_server, &client_addr, sizeof(struct sockaddr_in));

            // for simplicity
            assert(recv_msg->qesCount == 1);

            do{
                // step 1, cache
                bool all_hit = false;
                list_t *cache_rr = search_resouce_record(recv_msg,
                                                         dns_server->caches,
                                                         &all_hit,
                                                         NULL);
                if(all_hit){
                    // build the response packet and send to client
                    printf("udp finished resolving at step 1\n");

                    recv_msg->qr = 1;  // response
                    recv_msg->aa = 0;  // non-authoriative answer

                    build_response_msg(recv_msg, cache_rr, NULL, NULL);

                    //print_msg_site-info(dns_server, &query_server);
                    udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);

                    if(cache_rr != NULL){
                        free_list(cache_rr);
                    }
                    break;
                }else{
                    // free step 1's useless memory
                    node_t *head;
                    for(head = cache_rr->head; head != NULL; head = head->next){
                        free_resource_record((resource_record_t *)(head->data));
                    }
                    free_list(cache_rr);
                }

                // step 2, resouce record
                list_t *ans_list = search_resouce_record(recv_msg,
                                                         dns_server->rrs,
                                                         &all_hit, NULL);

                if(all_hit){
                    printf("udp finished resolving at step 2\n");

                    recv_msg->qr = 1;
                    recv_msg->aa = 1;

                    list_t *add_list = check_mx_answers(dns_server->rrs, ans_list);

                    build_response_msg(recv_msg, ans_list, NULL, add_list);
                    //print_msg_site-info(dns_server, &query_server);
                    udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);

                    // free the useless memory
                    if(ans_list != NULL){
                        free_list(ans_list);
                    }
                    node_t *free_node;
                    resource_record_t *free_rr;

                    for(free_node = add_list->head; free_node != NULL; free_node = free_node->next){
                        free_rr = (resource_record_t *)(free_node->data);
                        free_resource_record(free_rr);
                    }
                    free_list(add_list);

                    break;
                }

                // find NS type record
                list_t *aut_list = new_list();
                list_t *add_list = new_list();

                question_t *un_resolved = recv_msg->questions;
                
                node_t *ns_node, *a_node;
                for(ns_node = dns_server->rrs[NS_RR_type]->head;
                    ns_node != NULL;
                    ns_node = ns_node->next){

                    resource_record_t *ns_rr = (resource_record_t *)(ns_node->data);
                    if(child_domain(un_resolved->qName, ns_rr->rd_data.ns_record.name)){
                        for(a_node = dns_server->rrs[A_RR_type]->head;
                            a_node != NULL;
                            a_node = a_node->next){

                            resource_record_t *a_rr = (resource_record_t *)(a_node->data);
                            if(strcmp(ns_rr->rd_data.ns_record.name, 
                                      a_rr->name) == 0){
                                        
                                list_add_end(aut_list, (void *)copy_resource_record(ns_rr));
                                list_add_end(add_list, (void *)copy_resource_record(a_rr));
                            }
                        }
                    }
                }

                // step 3, if recursive and have enough information, then query again
                if(recv_msg->rd == 1 && dns_server->recursion_avb){
                    struct sockaddr_in ns_server;

                    list_t *tmp_list;
                    tmp_list = find_all_ns_ips(aut_list, add_list);
                    for(ip_node = tmp_list->head;
                        ip_node != NULL;
                        ip_node = ip_node->next){

                        list_add_end(ns_ips, (void *)(uint64_t)(ip_node->data));
                    }
                    free_list(tmp_list);
                    
                    assert(list_size(ns_ips) > 0);

                    uint32_t ns_ip = (uint32_t)(uint64_t)list_get_start(ns_ips);
                    list_add_end(dns_server->searched_ips, list_remove_start(ns_ips));

                    ns_server.sin_family = AF_INET;
                    // TODO
                    ns_server.sin_addr.s_addr = htonl(ns_ip);
                    ns_server.sin_port = htons(53);

                    //print_msg_site-info(dns_server, &ns_server);
                    udp_send_msg(dns_server->udp_sockfd, &ns_server, recv_msg);

                }else{
                    // merge all the rrs into query packet
                    recv_msg->qr = 1;
                    recv_msg->aa = 1;
                    build_response_msg(recv_msg, NULL, aut_list, add_list);
                    printf("THIS MESSAGE IS RESPONSE TO QUERY MSG:\n");
                    print_query(recv_msg);
                    //print_msg_site-info(dns_server, &query_server);
                    udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);
                    struct in_addr inaddr;
                    inaddr.s_addr = query_server.sin_addr.s_addr;
                    printf("send response to name-server(%s)\n", inet_ntoa(inaddr));
                }

                // free the useless memory
                if(ans_list != NULL){
                    free_list(ans_list);
                }
                if(aut_list != NULL){
                    free_list(aut_list);
                }
                if(add_list != NULL){
                    free_list(add_list);
                }

                printf("udp finished resolving at step 3\n");

            }while(0);
            printf("reached here 1\n");
        }else{
            printf("response message process\n");

            list_t *aut_list = new_list();
            list_t *add_list = new_list();
            resource_record_t *rr_node;

            for(rr_node = recv_msg->authorities; rr_node != NULL; rr_node = rr_node->next){
                if(rr_node->type == NS_RR_type){
                    list_add_end(aut_list, (void *)copy_resource_record(rr_node));
                }
            }
            for(rr_node = recv_msg->additionals; rr_node != NULL; rr_node = rr_node->next){
                if(rr_node->type == A_RR_type){
                    list_add_end(add_list, (void *)copy_resource_record(rr_node));
                }
            }

            list_t *tmp_list;
            tmp_list = find_all_ns_ips(aut_list, add_list);

            free_list(aut_list);
            free_list(add_list);

            for(ip_node = tmp_list->head;
                    ip_node != NULL;
                    ip_node = ip_node->next){

                list_add_end(ns_ips, (void *)(uint64_t)(ip_node->data));
            }
            free_list(tmp_list);
            
            if(recv_msg->qr == 1 && dns_server->recursion_avb){  // recursive query
                // if msg->ansCount == 0 and have enough information, then qeury again
                if(recv_msg->qesCount == 0){
                    
                    uint32_t ns_ip;
                    bool ok = false;
                    node_t *old_ip_node;
                    for(ip_node = ns_ips->head;
                        ip_node != NULL;
                        ip_node = ip_node->next){
                            
                        ok = false;
                        for(old_ip_node = dns_server->searched_ips->head;
                            old_ip_node != NULL;
                            old_ip_node = old_ip_node->next){
                                   
                            if((uint64_t)(ip_node->data) ==
                               (uint64_t)(old_ip_node->data)){
                                
                                ok = true;
                                break;
                            }
                        }
                        if(!ok){
                            ns_ip = (uint32_t)(uint64_t)(ip_node->data);
                            break;
                        }
                    }
                    if(!ok){
                        // send the response packet to query server
                        ns_server.sin_family = AF_INET;
                        ns_server.sin_addr.s_addr = (ns_ip);
                        ns_server.sin_port = htons(53);

                        recv_msg->qr = 0;
                        recv_msg->aa = 0;

                        message_t *tmp_msg = (message_t *)malloc(sizeof(message_t));
                        memcpy(tmp_msg, recv_msg, sizeof(message_t));

                        tmp_msg->autCount = 0;
                        tmp_msg->authorities = NULL;

                        tmp_msg->addCount = 0;
                        tmp_msg->additionals = NULL;

                        printf("RECURSIEVE QUERY:\n");
                        print_query(tmp_msg);

                        //print_msg_site-info(dns_server, &ns_server);
                        udp_send_msg(dns_server->udp_sockfd, &ns_server, tmp_msg);

                        free(tmp_msg);

                    }else{
                        if(dns_server->start_ns_flag){
                            message_t *dns_response_msg = (message_t *)malloc(sizeof(message_t));
                            decode_message(dns_response_msg, packet_buffer, packet_size);

                            pthread_mutex_lock(&dns_server->lock);
                            dns_server->dns_response_msg = dns_response_msg;
                            dns_server->notify_flag = true;
                            pthread_cond_signal(&dns_server->notify);

                            free_list(dns_server->searched_ips);
                            dns_server->searched_ips = new_list();

                            pthread_mutex_unlock(&dns_server->lock);
                        }else{
                            //print_msg_site-info(dns_server, &query_server);
                            udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);
                        }
                    }
                }else{
                    if(dns_server->start_ns_flag){
                        message_t *dns_response_msg = (message_t *)malloc(sizeof(message_t));
                        decode_message(dns_response_msg, packet_buffer, packet_size);

                        pthread_mutex_lock(&dns_server->lock);
                        dns_server->dns_response_msg = dns_response_msg;
                        dns_server->notify_flag = true;
                        pthread_cond_signal(&dns_server->notify);

                        free_list(dns_server->searched_ips);
                        dns_server->searched_ips = new_list();

                        pthread_mutex_unlock(&dns_server->lock);
                    }else{
                        //print_msg_site-info(dns_server, &query_server);
                        udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);
                    }
                }
            }else{  // iterative query
                if(dns_server->start_ns_flag){
                    if(recv_msg->ansCount == 0){

                        uint32_t ns_ip;
                        bool ok = false;
                        node_t *old_ip_node;
                        for(ip_node = ns_ips->head;
                                ip_node != NULL;
                                ip_node = ip_node->next){

                            ok = false;
                            for(old_ip_node = dns_server->searched_ips->head;
                                    old_ip_node != NULL;
                                    old_ip_node = old_ip_node->next){

                                if((uint64_t)(ip_node->data) == (uint64_t)(old_ip_node->data)){
                                    ok = true;
                                    break;
                                }
                            }
                            if(!ok){
                                ns_ip = (uint32_t)(uint64_t)(ip_node->data);
                                break;
                            }
                        }
                        list_add_end(dns_server->searched_ips, (void *)(uint64_t)ns_ip);
                        printf("ok = %d, ns_ip = %x\n", ok, ns_ip);
                        if(!ok){
                            // send the response packet to other name server
                            ns_server.sin_family = AF_INET;
                            ns_server.sin_addr.s_addr = htonl(ns_ip);
                            ns_server.sin_port = htons(53);

                            recv_msg->qr = 0;
                            recv_msg->aa = 0;

                            message_t *tmp_msg = (message_t *)malloc(sizeof(message_t));
                            memcpy(tmp_msg, recv_msg, sizeof(message_t));

                            tmp_msg->autCount = 0;
                            tmp_msg->authorities = NULL;

                            tmp_msg->addCount = 0;
                            tmp_msg->additionals = NULL;

                            printf("ITERATIVE QUERY:\n");
                            print_query(tmp_msg);

                            //print_msg_site-info(dns_server, &ns_server);
                            udp_send_msg(dns_server->udp_sockfd, &ns_server, tmp_msg);

                            free(tmp_msg);

                        }else{
                            // no other name server, send the packet to tcp server
                            // although it has no answers
                            message_t *dns_response_msg = (message_t *)malloc(sizeof(message_t));
                            decode_message(dns_response_msg, packet_buffer, packet_size);

                            pthread_mutex_lock(&dns_server->lock);
                            dns_server->dns_response_msg = dns_response_msg;
                            dns_server->notify_flag = true;
                            pthread_cond_signal(&dns_server->notify);

                            free_list(dns_server->searched_ips);
                            dns_server->searched_ips = new_list();

                            pthread_mutex_unlock(&dns_server->lock);
                            
                        }

                    }else{
                        message_t *dns_response_msg = (message_t *)malloc(sizeof(message_t));
                        decode_message(dns_response_msg, packet_buffer, packet_size);

                        pthread_mutex_lock(&dns_server->lock);
                        dns_server->dns_response_msg = dns_response_msg;
                        dns_server->notify_flag = true;
                        pthread_cond_signal(&dns_server->notify);

                        free_list(dns_server->searched_ips);
                        dns_server->searched_ips = new_list();

                        pthread_mutex_unlock(&dns_server->lock);
                    }
                }else{
                    //print_msg_site-info(dns_server, &query_server);
                    udp_send_msg(dns_server->udp_sockfd, &query_server, recv_msg);
                }
            }
        }
        free_message(recv_msg);
    }
    return NULL;
}

static list_t *find_all_ns_ips(list_t *aut_list, list_t *add_list){
    node_t *ns_node, *a_node;
    resource_record_t *ns_rr, *a_rr;

    list_t *ip_list = new_list();

    int i, ip_value, ip_base;
    for(ns_node = aut_list->head; ns_node != NULL; ns_node = ns_node->next){
        ns_rr = (resource_record_t *)(ns_node->data);

        if(ns_rr->type != NS_RR_type){
            continue;
        }
        for(a_node = add_list->head; a_node != NULL; a_node = a_node->next){
            a_rr = (resource_record_t *)(a_node->data);

            if(strcmp(ns_rr->rd_data.ns_record.name, a_rr->name) == 0){
                ip_value = 0;
                ip_base = 1;
                for(i = 0; i < 4; ++i){
                    ip_value += a_rr->rd_data.a_record.addr[i] * ip_base;
                    ip_base *= 256;
                }
                list_add_end(ip_list, (void *)(uint64_t)htonl(ip_value));
            }
        }
    }
    return ip_list;
}

void *tcp_server(void *parm){
    printf("Start up tcp server ...\n");
    dns_server_t *dns_server = (dns_server_t *)parm;

    // bind
    if(bind(dns_server->tcp_sockfd, (struct sockaddr *)&(dns_server->sockaddr), sizeof(dns_server->sockaddr)) < 0){
        perror("bind");
        return NULL;
    }
    LOG("tcp_server thread: bind succeed\n");

    // listen
    if(listen(dns_server->tcp_sockfd, SOMAXCONN) < 0){
        perror("listen");
        return NULL;
    }
    LOG("tcp_server thread: waiting for DNS-client's connection\n");

    // define related variables
    int client_sockfd;
    struct sockaddr_in client_addr;
    socklen_t client_size = sizeof(client_addr);

    message_t *query_msg = NULL;
    for(;;){
        if((client_sockfd = accept(dns_server->tcp_sockfd, (struct sockaddr *)&client_addr, &client_size)) < 0){
            perror("accpet");
            continue;
        }
        LOG("tcp_server: accepted a new connection request\n");

        uint16_t bin_msg_size;
        uint8_t *bin_msg = smart_read(client_sockfd, &bin_msg_size);

        // set start_ns_flag
        dns_server->start_ns_flag = true;

        // init searched_ips
        free_list(dns_server->searched_ips);
        dns_server->searched_ips = new_list();

        // decode the binary packet
        query_msg = (message_t *)calloc(1, sizeof(message_t));
        decode_message(query_msg, bin_msg, bin_msg_size);

        printf("TCP SERVER RECEIVED A MSG:\n");
        print_query(query_msg);

        assert(query_msg->qr == 0);

        // step 1, hit the cache
        bool all_hit = false;

        node_t *head = NULL;
        list_t *hit_status = NULL;
        list_t *cache_rr = search_resouce_record(query_msg, dns_server->caches, &all_hit, NULL);

        do{
            if(all_hit){
                printf("tcp finished resolving at step 1\n");
                // build the response packet and send to client
                query_msg->qr = 1;  // response
                query_msg->aa = 0;  // non-authoriative answer
                build_response_msg(query_msg, cache_rr, NULL, NULL);

                smart_write(client_sockfd, query_msg);

                if(cache_rr != NULL){
                    free_list(cache_rr);
                }
                break;
            }else{
                // free step 1's useless memory
                for(head = cache_rr->head; head != NULL; head = head->next){
                    free_resource_record((resource_record_t *)(head->data));
                }
                free_list(cache_rr);
            }

            // step 2, search the resource record when step 1 failed
            hit_status = new_list();
            list_t *ans_list = search_resouce_record(query_msg, dns_server->rrs, &all_hit, hit_status);

            if(all_hit){
                printf("tcp finished resolving at step 2\n");

                query_msg->qr = 1;
                query_msg->aa = 1;

                build_response_msg(query_msg, ans_list, NULL, NULL);
                smart_write(client_sockfd, query_msg);

                // free the useless memory
                free_list(hit_status);

                if(ans_list != NULL){
                    free_list(ans_list);
                }
                break;
            }

            // step 3, ask other name server's help
            // when some question can't answer by itself

            list_t *aut_list = new_list();
            list_t *add_list = new_list();
            
            int idx;
            question_t *un_resolved = NULL;
            printf("list_size(hit_status) = %d\n", list_size(hit_status));
            for(head = hit_status->head;
                    head != NULL;
                    head = head->next){
                
                printf("head->data = %d\n", (bool)(head->data));
            }
            for(head = hit_status->head,
                    un_resolved = query_msg->questions;
                    head != NULL;
                    head = head->next,
                    un_resolved = un_resolved->next){

                if((bool)(head->data)){
                    // this question is resolvedd by itself
                    continue;
                }
                // this question need ask other name server
                // for simplicity, we use the SBELT
                ask_other_name_server(query_msg, un_resolved, dns_server);

                // wait the response
                pthread_mutex_lock(&dns_server->lock);
                while(!dns_server->notify_flag){
                    pthread_cond_wait(&dns_server->notify, &dns_server->lock);
                }
                dns_server->notify_flag = false;
                pthread_mutex_unlock(&dns_server->lock);

                // process the response
                add_msg_rr_into_list(ans_list, dns_server->dns_response_msg->answers);
                dns_server->dns_response_msg->answers = NULL;

                add_msg_rr_into_list(aut_list, dns_server->dns_response_msg->authorities);
                dns_server->dns_response_msg->authorities = NULL;

                add_msg_rr_into_list(add_list, dns_server->dns_response_msg->additionals);
                dns_server->dns_response_msg->additionals = NULL;
            }

            free_message(dns_server->dns_response_msg);

            printf("tcp finished resolving at step 3\n");

            // merge all the rrs into query packet
            query_msg->qr = 1;
            query_msg->aa = 1;
            build_response_msg(query_msg, ans_list, aut_list, add_list);
            
            smart_write(client_sockfd, query_msg);

            // set the start_ns_flag
            dns_server->start_ns_flag = false;

            // free the useless memory
            free_list(hit_status);

            if(ans_list != NULL){
                free_list(ans_list);
            }
            if(aut_list != NULL){
                free_list(aut_list);
            }
            if(add_list != NULL){
                free_list(add_list);
            }
        }while(0);

        free_message(query_msg);
    }
}

static int ask_other_name_server(message_t *query_msg, question_t *q, dns_server_t *dns_server){
    struct sockaddr_in servaddr;
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr((char *)(list_get_start(dns_server->SBELT)));
    servaddr.sin_port = htons(53);

    list_add_end(dns_server->searched_ips, (void *)(uint64_t)inet_addr((char *)(list_get_start(dns_server->SBELT))));

    message_t *new_query_msg = (message_t *)calloc(1, sizeof(message_t));
    memcpy(new_query_msg, query_msg, sizeof(message_t));

    new_query_msg->qesCount = 1;
    new_query_msg->questions = copy_question(q);

    //print_msg_site-info(dns_server, &servaddr);
    udp_send_msg(dns_server->udp_sockfd, &servaddr, new_query_msg);
}

static int build_response_msg(message_t *query_msg, list_t *ans_list, list_t *aut_list, list_t *add_list){
    assert(query_msg->answers == NULL);
    assert(query_msg->authorities == NULL);
    assert(query_msg->additionals == NULL);

    node_t *head = NULL;
    resource_record_t **pp_rr = NULL;
    if(ans_list != NULL){
        query_msg->ansCount = list_size(ans_list);

        pp_rr = &query_msg->answers;
        for(head = ans_list->head; head != NULL; head = head->next){
            *pp_rr = (resource_record_t *)(head->data);
            pp_rr = &((*pp_rr)->next);

            *pp_rr = NULL;  // make sure the last one's next field is NULL
        }
    }
    if(aut_list != NULL){
        query_msg->autCount = list_size(aut_list);

        pp_rr = &query_msg->authorities;
        for(head = aut_list->head; head != NULL; head = head->next){
            *pp_rr = (resource_record_t *)(head->data);
            pp_rr = &((*pp_rr)->next);

            *pp_rr = NULL;  // make sure the last one's next field is NULL
        }
    }
    if(add_list != NULL){
        query_msg->addCount = list_size(add_list);

        pp_rr = &query_msg->additionals;
        for(head = add_list->head; head != NULL; head = head->next){
            *pp_rr = (resource_record_t *)(head->data);
            pp_rr = &((*pp_rr)->next);

            *pp_rr = NULL;  // make sure the last one's next field is NULL
        }
    }
    return 0;
}

static int add_msg_rr_into_list(list_t *list, resource_record_t *rr){
    assert(list != NULL);
    resource_record_t *tmp = NULL;
    while(rr != NULL){
        tmp = rr;
        rr = rr->next;

        tmp->next = NULL;
        list_add_end(list,tmp);
    }
    return 0;
}

static list_t *search_resouce_record(message_t *query_msg, list_t **caches, bool *all_hit, list_t *question_status){
    question_t *q = query_msg->questions;

    node_t *node = NULL;
    resource_record_t *rr = NULL;

    list_t *list = new_list();

    int hit_count = 0;
    bool hit_flag;
    while(q != NULL){
        hit_flag = false;

        node = NULL;
        for(node = caches[q->qType]->head; node != NULL; node = node->next){
            rr = (resource_record_t  *)(node->data);
            if(rr->class == q->qClass && strcmp(rr->name, q->qName) == 0){
                list_add_end(list, (void *)copy_resource_record(rr));
                hit_flag = true;
            }
        }

        if(hit_flag){
            ++hit_count;
            if(question_status != NULL){
                list_add_end(question_status, (void *)true);
            }
        }else{
            if(question_status != NULL){
                list_add_end(question_status, (void *)false);
            }
        }
        q = q->next;
    }
    *all_hit = (query_msg->qesCount == hit_count ? true : false);

    return list;
}

static int configure_dns_server(dns_server_t *dns_server, const char *conf_file){
    FILE *fp = fopen(conf_file, "r");
    if(fp == NULL){
        printf("fopen file(%s) failed\n", conf_file);
        return -1;
    }

    // init dns_server's SBELT field
    dns_server->SBELT = new_list();

    char *line_buf = (char *)malloc(sizeof(char) * LINE_BUF_SIZE);
    char *pch, *name, *value;
    int idx;

    while(fgets(line_buf, LINE_BUF_SIZE, fp) != NULL){
        idx = 0;
        pch = strtok(line_buf, ": \n");
        while(pch != NULL){
            assert(idx < 2);
            if(idx == 0){
                // configure name
                name = pch;
            }else{
                // configure value
                value = pch;
            }
            ++idx;
            pch = strtok(NULL, ": \n");
        }
        if(strcmp(name, "ip") == 0){
            dns_server->ip_address = strdup(value);
        }else if(strcmp(name, "port") == 0){
            dns_server->port = (uint16_t)atoi(value);
        }else if(strcmp(name, "SBELT") == 0){
            list_add_end(dns_server->SBELT, strdup(value));
        }else if(strcmp(name, "Recursion") == 0){
            if(value[0] == 'Y' || value[0] == 'y'){
                dns_server->recursion_avb = true;
            }else{
                dns_server->recursion_avb = false;
            }
        }else if(strcmp(name, "desc") == 0){
            dns_server->dns_server_desc = strdup(value);
        }else{
            printf("Unknow setting values");
        }
    }
    // set the start_ns_flag
    dns_server->start_ns_flag = false;

    // init tcp and udp resource
    dns_server->tcp_sockfd = socket(AF_INET, SOCK_STREAM, 0);
    dns_server->udp_sockfd = socket(AF_INET, SOCK_DGRAM, 0);

    dns_server->sockaddr.sin_family = AF_INET;
    dns_server->sockaddr.sin_addr.s_addr = inet_addr(dns_server->ip_address);
    dns_server->sockaddr.sin_port = htons(dns_server->port);

    // init synchronization resource
    dns_server->notify_flag = false;
    pthread_cond_init(&dns_server->notify, NULL);
    pthread_mutex_init(&dns_server->lock, NULL);

    dns_server->dns_response_msg = NULL;

    dns_server->searched_ips = new_list();

    free(line_buf);
    fclose(fp);

    return 0;
}

static int read_resource_record(list_t ***rrs, const char *file){
    FILE *fp = fopen(file, "r");
    if(fp == NULL){
        printf("fopen file(%s) failed\n", file);
        return -1;
    }
    
    *rrs = (list_t **)malloc(sizeof(list_t *) * RR_TYPE_NUM);
    int i;
    for(i = 0; i < RR_TYPE_NUM; ++i){
        (*rrs)[i] = new_list();
    }

    char *line_buf = (char *)malloc(sizeof(char) * LINE_BUF_SIZE);
    char *pch;
    int idx;
    resource_record_t *rr = NULL;
    while(fgets(line_buf, LINE_BUF_SIZE, fp) != NULL){
        rr = (resource_record_t *)calloc(1, sizeof(resource_record_t));
        rr->next = NULL;
        idx = 0;
        pch = strtok(line_buf, " ,\n");
        while(pch != NULL){
            assert(idx < 5);
            switch(idx){
                case 0: // DNS-Name
                    if(strcmp(pch, ".") == 0){
                        rr->name = strdup("");  // Root
                    }else{
                        rr->name = strdup(pch);
                    }
                    break;
                case 1: // TTL
                    rr->ttl = (uint32_t)atol(pch);
                    break;
                case 2:
                    rr->class = get_rr_class_value(pch);
                    break;
                case 3:
                    rr->type = get_rr_type_value(pch);
                    break;
                case 4:
                    set_rr_rd_data(rr, pch);
                    break;
            }
            ++idx;
            pch = strtok(NULL, " ,\n");
        }
        // add this rr into assigned resource record list
        list_add_end((*rrs)[rr->type], rr);
    }

    free(line_buf);
    fclose(fp);

    return 0;
}

void print_dns_server(dns_server_t *dns_server){
    printf("%s\n", dns_server->dns_server_desc);
    printf("    IP      : %s\n", dns_server->ip_address);
    printf("    PORT    : %u\n", dns_server->port);
    printf("    SBELT   : %s\n", (char *)(list_get_start(dns_server->SBELT)));
    printf("    RA Flag : %s\n", dns_server->recursion_avb ? "True" : "False");
    int i, j;
    list_t *list = NULL;
    node_t *node = NULL;
    printf("Resource Record:\n");
    for(i = 0, j = 0; i < RR_LIST_SIZE; ++i){
        list = dns_server->rrs[RR_LIST[i]];
        for(node = list->head; node != NULL; node = node->next){
            print_resource_record((resource_record_t *)node->data);
            j++;
        }
    } 
    if(j == 0){
        printf("    NULL\n");
    }
    printf("Caches:\n");
    for(i = 0, j = 0; i < RR_LIST_SIZE; ++i){
        list = dns_server->caches[RR_LIST[i]];
        for(node = list->head; node != NULL; node = node->next){
            print_resource_record((resource_record_t *)node->data);
            j++;
        }
    } 
    if(j == 0){
        printf("    NULL\n");
    }
}

static bool child_domain(const char *parent, const char *child){
    // parent = "教育.中国", child = "中国."
    int i = strlen(parent);
    int j = strlen(child);

    if(j == 0){
        // child = ""
        return true;
    }else{
        if(i < j){
            return false;
        }else{
            while(i > 0 && j > 0){
                if(parent[--i] != child[--j]){
                    return false;
                }
            }
            printf("return true\n");
            return true;
        }
    }
}

static void print_msg_site_info(dns_server_t *dns_server, struct sockaddr_in *server_addr){
    struct in_addr from, to;
    from.s_addr = dns_server->sockaddr.sin_addr.s_addr;
    to.s_addr = server_addr->sin_addr.s_addr;

    printf("Send packet from (%s) to (%s)\n", inet_ntoa(from), inet_ntoa(to));
}
