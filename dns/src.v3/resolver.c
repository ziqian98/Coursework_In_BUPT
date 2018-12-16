#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>

#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "log.h"
#include "list.h"
#include "dns-msg-lib.h"
#include "smart-read-write.h"

static const uint16_t DNS_PORT = 53;
static const uint16_t DNS_PACKET_ID = 0xbeef;

#define BUF_SIZE 2048

static uint8_t DNS_MSG_BUF[BUF_SIZE];

// static char *TYPE_NAME[] = {
//     "A",
//     "NS",
//     "CNAME",
//     "PTR",
//     "MX"
// };
// 
// static int TYPE_VALUE[] = { 1, 2, 5, 12, 15 };

static question_t *parse_question(const char *arg);

static int build_msg_from_args(message_t *msg, int argc, char *argv[], char **ip);

int main(int argc, char *argv[]){
    char *name_server_ip = NULL;
    message_t *msg = (message_t *)calloc(1, sizeof(message_t));
    build_msg_from_args(msg, argc, argv, &name_server_ip);
    print_query(msg);

    /* create a socket address */
    struct sockaddr_in server_addr;
    int sockfd;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(DNS_PORT);
    server_addr.sin_addr.s_addr = inet_addr(name_server_ip);


    if(connect(sockfd, (const struct sockaddr *)&server_addr, sizeof(server_addr)) < 0){
        perror("connect\n");
        free_message(msg);
        return -1;
    }

    smart_write(sockfd, msg);

    free_message(msg);

    LOG("send dns packet succeed\n");

    uint16_t response_bin_packet_size;
    uint8_t *response_bin_packet = smart_read(sockfd, &response_bin_packet_size);

    msg = (message_t *)calloc(1, sizeof(message_t));
    decode_message(msg, response_bin_packet, response_bin_packet_size);

    assert(msg->qr == 1);
    print_query(msg);

    return 0;
}

static int build_msg_from_args(message_t *msg, int argc, char *argv[], char **ip){
    int i;
    char *query_server_ip = NULL;
    list_t *list_qest = new_list();

    for(i = 1; i < argc; ++i){
        if(argv[i][0] == '-'){
            continue;
        }
        if(argv[i][0] == '@'){  // Name Server's IP address
            query_server_ip = argv[i];
            *ip = argv[i] + 1;
        }else{  // @Domain Name
            question_t *q = parse_question(argv[i]);
            list_add_end(list_qest, q);
        }
    }

    // Recursion Desired
    for(i = 1; i < argc; ++i){
        if(argv[i][0] == '-' && strcmp(argv[i], "-RD") == 0){
            msg->rd = 1;
            break;
        }
    }

    // id field
    msg->id = DNS_PACKET_ID;
    // qr field
    // msg->qr = 0;    // initialized in calloc function

    node_t *head = NULL;
    if(list_size(list_qest) == 0 || query_server_ip == NULL){
        printf("dns-client-tcp @query-server-ip content=XXX,type=[-A|-CNAME|-MX|-PTR],RA=[true|false]\n");
        for(head = list_qest->head; head != NULL; head = head->next){
            question_t *tmp = (question_t *)(head->data);
            free(tmp->qName);
            free(head->data);
        }
        free_list(list_qest);
        return -1;
    }
    
    // add all the questions into msg
    question_t **pp_qest = &msg->questions;
    for(head = list_qest->head; head != NULL; head = head->next){
            msg->qesCount += 1;
            (*pp_qest) = head->data;
            pp_qest = &(*pp_qest)->next;
    }

    free_list(list_qest);
}

static question_t *parse_question(const char *arg){
    char *str = strdup(arg);

    int idx;
    char *pch, *name, *value;

    list_t *settings = new_list();

    pch = strtok(str, ",");
    while(pch != NULL){
        list_add_end(settings, (void *)pch);
        pch = strtok(NULL, ",");
    }

    question_t *q = (question_t *)calloc(1, sizeof(question_t));

    node_t *head = NULL;
    for(head = settings->head; head != NULL; head = head->next){
        pch = strtok((char *)head->data, "=");
        idx = 0;
        while(pch != NULL){
            assert(idx < 2);
            if(idx == 0){
                name = pch;
            }else{
                value = pch;
            }
            pch = strtok(NULL, ",");
            ++idx;
        }
        // allocate a message

        // default value
        q->qClass = IN_RR_class;

        if(strcmp(name, "content") == 0){
            q->qName = strdup(value);
        }else if(strcmp(name, "type") == 0){
            q->qType = get_rr_type_value(value);
            printf("+ Using %s query type\n", value);
        }else{
            printf("Unknown settings");
        }
        // PTR type
        if(q->qType == PTR_RR_type){
            char *ptr_name = (char *)malloc(sizeof(char) * (strlen(q->qName) + strlen(".组织") + 1));
            strcpy(ptr_name, q->qName);
            strcat(ptr_name, ".组织");
            free(q->qName);
            q->qName = ptr_name;
        }
    }

    free(str);
    free_list(settings);

    return q;
}
