#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>

#include "dns-msg-lib.h"
#include "log.h"

static const uint32_t QR_MASK       = 0x8000;
static const uint32_t OPCODE_MASK   = 0x7800;
static const uint32_t AA_MASK       = 0x0400;
static const uint32_t TC_MASK       = 0x0200;
static const uint32_t RD_MASK       = 0x0100;
static const uint32_t RA_MASK       = 0x0080;
static const uint32_t RCODE_MASK    = 0x000F;

uint16_t get_rr_class_value(const char *str){
    if(strcmp(str, "IN") == 0){
        return IN_RR_class;
    }
    return 1;
}

uint16_t get_rr_type_value(const char *str){
    if(strcmp(str, "A") == 0){
        return A_RR_type;
    }else if(strcmp(str, "NS") == 0){
        return NS_RR_type;
    }else if(strcmp(str, "CNAME") == 0){
        return CNAME_RR_type;
    }else if(strcmp(str, "PTR") == 0){
        return PTR_RR_type;
    }else if(strcmp(str, "MX") == 0){
        return MX_RR_type;
    }
    return A_RR_type;
}

void set_rr_rd_data(resource_record_t *ptr_rr, char *str){
    char *pch;
    int idx;
    if(ptr_rr->type == A_RR_type){
        idx = 0;
        pch = strtok(str, ".");
        while(pch != NULL){
            ptr_rr->rd_data.a_record.addr[idx++] = (uint8_t)atoi(pch);
            pch = strtok(NULL, ".");
        }

    }else if(ptr_rr->type == NS_RR_type){
        ptr_rr->rd_data.ns_record.name = strdup(str);

    }else if(ptr_rr->type == CNAME_RR_type){
        ptr_rr->rd_data.cname_record.name = strdup(str);

    }else if(ptr_rr->type == PTR_RR_type){
        ptr_rr->rd_data.ptr_record.name = strdup(str);

    }else if(ptr_rr->type == MX_RR_type){
        ptr_rr->rd_data.mx_record.preference = 10;  // default
        ptr_rr->rd_data.mx_record.exchange = strdup(str);
    }
}

static void encode_domain_name(uint8_t **buffer, const char *domain);
static char *decode_domain_name(uint8_t **buffer);

static void encode_header(message_t *msg, uint8_t **buffer);
static void decode_header(message_t *msg, uint8_t **buffer);

static int encode_resource_record(resource_record_t *rr, uint8_t **buffer);
static int decode_resource_record(resource_record_t *rr, uint8_t **buffer);

int encode_message(message_t *msg, uint8_t **buffer){
    question_t *q;
    int rc;

    encode_header(msg, buffer);

    q = msg->questions;

    while(q){
        encode_domain_name(buffer, q->qName);
        put16bits(buffer, q->qType);
        put16bits(buffer, q->qClass);

        q = q->next;
    }

    rc = 0;
    rc |= encode_resource_record(msg->answers, buffer);
    rc |= encode_resource_record(msg->authorities, buffer);
    rc |= encode_resource_record(msg->additionals, buffer);

    return rc;
}

int decode_message(message_t *msg, uint8_t *buffer, int size){
    int i;
    decode_header(msg, &buffer);
    
    // parse question
    uint32_t qes_count = msg->qesCount;

    question_t *question = NULL;
    question_t **pp_question = &question;

    for(i = 0; i < qes_count; ++i){
        (*pp_question) = (question_t *)malloc(sizeof(question_t));
        (*pp_question)->qName  = decode_domain_name(&buffer);
        (*pp_question)->qType  = get16bits(&buffer);
        (*pp_question)->qClass = get16bits(&buffer);
        (*pp_question)->next   = NULL;
        
        pp_question = &((*pp_question)->next);
    }
    msg->questions = question;

    // answer section
    uint32_t other_counter = msg->ansCount;
    resource_record_t *p_rr = NULL;
    resource_record_t **pp_rr = &p_rr;

    for(i = 0; i < other_counter; ++i){
        *pp_rr = (resource_record_t *)malloc(sizeof(resource_record_t));
        decode_resource_record(*pp_rr, &buffer);

        (*pp_rr)->next = NULL;
        pp_rr = &(*pp_rr)->next;
    }
    msg->answers = p_rr;

    // authorities section
    other_counter = msg->autCount;
    p_rr = NULL;
    pp_rr = &p_rr;

    for(i = 0; i < other_counter; ++i){
        *pp_rr = (resource_record_t *)malloc(sizeof(resource_record_t));
        decode_resource_record(*pp_rr, &buffer);

        (*pp_rr)->next = NULL;
        pp_rr = &(*pp_rr)->next;
    }
    msg->authorities = p_rr;

    // additionals section
    other_counter = msg->addCount;
    p_rr = NULL;
    pp_rr = &p_rr;

    for(i = 0; i < other_counter; ++i){
        *pp_rr = (resource_record_t *)malloc(sizeof(resource_record_t));
        decode_resource_record(*pp_rr, &buffer);

        (*pp_rr)->next = NULL;
        pp_rr = &(*pp_rr)->next;
    }
    msg->additionals = p_rr;

    return 0;
}

uint8_t get8bits(uint8_t **buffer){
    uint8_t value;
    memcpy(&value, *buffer, 1);
    *buffer += 1;

    return value;
}

uint16_t get16bits(uint8_t **buffer){
    uint16_t value;
    memcpy(&value, *buffer, 2);
    *buffer += 2;

    return ntohs(value);
}

uint32_t get32bits(uint8_t **buffer){
    uint32_t value;
    memcpy(&value, *buffer, 4);
    *buffer += 4;

    return ntohl(value);
}

void put8bits(uint8_t **buffer, uint8_t value){
    memcpy(*buffer, &value, 1);
    *buffer += 1;
}

void put16bits(uint8_t **buffer, uint16_t value){
    value = htons(value);
    memcpy(*buffer, &value, 2);
    *buffer += 2;
}

void put32bits(uint8_t **buffer, uint32_t value){
    value = htonl(value);
    memcpy(*buffer, &value, 4);
    *buffer += 4;
}

static void encode_domain_name(uint8_t **buffer, const char *domain){
    uint8_t *buf = *buffer;
    const char *beg = domain;
    const char *pos = NULL;
    int len = 0;
    int i = 0;

    if(strlen(domain) == 0){
        buf[i++] = 0;
        *buffer += i;
        return ;
    }

    while((pos = strchr(beg, '.'))){
        len = pos - beg;
        buf[i] = (uint8_t)len;
        i += 1;
        memcpy(buf + i, beg, len);
        i += len;
        
        beg = pos + 1;
    }

    // the last domain components
    len = strlen(domain) - (beg - domain);

    buf[i++] = (uint8_t)len;

    memcpy(buf + i, beg, len);
    i += len;

    buf[i++] = 0;
    *buffer += i;
}

static char *decode_domain_name(uint8_t **buffer){
    char *name = (char *)malloc(sizeof(char) * 256);
    const uint8_t *buf = *buffer;
    int i = 0, j = 0;

    while(buf[i] != 0){
        if(i != 0){
            name[j++] = '.';
        }
        uint8_t sub_length = buf[i++];
        
        memcpy(name + j, buf + i, sub_length);
        i += sub_length;
        j += sub_length;
    }
    name[j] = '\0';

    *buffer += i + 1;   // jump over the last zero(0)

    char *ret = strdup(name);
    free(name);
    
    return ret;
}

static void encode_header(message_t *msg, uint8_t **buffer){
    put16bits(buffer, msg->id);

    uint32_t field = 0;
    field |= (msg->qr << 15) & QR_MASK;
    field |= (msg->opcode << 11) & OPCODE_MASK;
    field |= (msg->aa << 10) & AA_MASK;
    field |= (msg->tc << 9) & TC_MASK;
    field |= (msg->rd << 8) & RD_MASK;
    field |= (msg->ra << 7) & RA_MASK;
    field |= (msg->rcode  << 0) & RCODE_MASK;

    put16bits(buffer, (uint16_t)field);
    put16bits(buffer, msg->qesCount);
    put16bits(buffer, msg->ansCount);
    put16bits(buffer, msg->autCount);
    put16bits(buffer, msg->addCount);
}

static void decode_header(message_t *msg, uint8_t **buffer){
    msg->id = get16bits(buffer);

    uint32_t fields = get16bits(buffer);
    msg->qr     = (fields & QR_MASK)     >> 15;
    msg->opcode = (fields & OPCODE_MASK) >> 11;
    msg->aa     = (fields & AA_MASK)     >> 10;
    msg->tc     = (fields & TC_MASK)     >> 9;
    msg->rd     = (fields & RD_MASK)     >> 8;
    msg->ra     = (fields & RA_MASK)     >> 7;
    msg->rcode  = (fields & RCODE_MASK)  >> 0;

    msg->qesCount = get16bits(buffer);
    msg->ansCount = get16bits(buffer);
    msg->autCount = get16bits(buffer);
    msg->addCount = get16bits(buffer);
}

static int encode_resource_record(resource_record_t *rr, uint8_t **buffer){
    uint8_t *buf = NULL, *p_rd_length = NULL;
    int i;
    while(rr){
        encode_domain_name(buffer, rr->name);
        put16bits(buffer, rr->type);
        put16bits(buffer, rr->class);
        put32bits(buffer, rr->ttl);

        p_rd_length = *buffer;
        put16bits(buffer, rr->rd_length);

        switch(rr->type){
            case A_RR_type:
                for(i = 0; i < 4; ++i){
                    put8bits(buffer, rr->rd_data.a_record.addr[i]);
                }
                put16bits(&p_rd_length, 4);
                break;

            case CNAME_RR_type:
                buf = *buffer;
                encode_domain_name(buffer, rr->rd_data.cname_record.name);
                put16bits(&p_rd_length, *buffer - buf);
                break;

            case MX_RR_type:
                buf = *buffer;
                put16bits(buffer, rr->rd_data.mx_record.preference);
                encode_domain_name(buffer, rr->rd_data.mx_record.exchange);
                put16bits(&p_rd_length, *buffer - buf);
                break;

            case NS_RR_type:
                buf = *buffer;
                encode_domain_name(buffer, rr->rd_data.ns_record.name);
                put16bits(&p_rd_length, *buffer - buf);
                break;

            case PTR_RR_type:
                buf = *buffer;
                encode_domain_name(buffer, rr->rd_data.ptr_record.name);
                put16bits(&p_rd_length, *buffer - buf);
                break;
            default:
                printf("Unknonw type %u", rr->type);
                return 1;
        }
        rr = rr->next;
    }
    return 0;
}

static int decode_resource_record(resource_record_t *rr, uint8_t **buffer){
    rr->name = decode_domain_name(buffer);

    rr->type        = get16bits(buffer);
    rr->class       = get16bits(buffer);
    rr->ttl         = get32bits(buffer);
    rr->rd_length   = get16bits(buffer);

    int i;
    switch(rr->type){
        case A_RR_type:
            for(i = 0; i < 4; ++i){
                rr->rd_data.a_record.addr[i] = get8bits(buffer);
            }
            break;

        case CNAME_RR_type:
            rr->rd_data.cname_record.name = decode_domain_name(buffer);
            break;

        case MX_RR_type:
            rr->rd_data.mx_record.preference = get16bits(buffer);
            rr->rd_data.mx_record.exchange = decode_domain_name(buffer);
            break;

        case NS_RR_type:
            rr->rd_data.ns_record.name = decode_domain_name(buffer);
            break;

        case PTR_RR_type:
            rr->rd_data.ptr_record.name = decode_domain_name(buffer);
            break;
        default:
            printf("Unknonw type %u", rr->type);
            return 1;
    }
    return 0;
}

void print_query(message_t *msg){
    printf("DNS packet message:\n");
    printf("    QUERY ID         : %02x\n", msg->id);
    printf("    FIELDS           : [QR: %u, AA: %u, RD: %u, RA: %u, OPCode: %u ]\n", msg->qr, msg->aa, msg->rd, msg->ra, msg->opcode);
    printf("    QuestionsCount   : %u\n", msg->qesCount);
    printf("    AnswersCount     : %u\n", msg->ansCount);
    printf("    AuthoritiesCount : %u\n", msg->autCount);
    printf("    AdditionalsCount : %u\n", msg->addCount);

    question_t *q = msg->questions;
    while(q){
        printf("    ******** Qeustion    ********\n");
        printf("        qName  : '%s'\n", q->qName);
        printf("        qType  : %u\n", q->qType);
        printf("        qClass : %u\n", q->qClass);
        
        q = q->next;
    }
    printf("    ******** Answers     ********:\n");
    print_resource_record(msg->answers);
    printf("    ******** Authorities ********:\n");
    print_resource_record(msg->authorities);
    printf("    ******** Additionals ********:\n");
    print_resource_record(msg->additionals);
    printf("\n");
}

void print_resource_record(resource_record_t *rr){
    int i;
    while(rr){
        printf("        name      : '%s'\n", rr->name);
        printf("        type      : %u\n", rr->type);
        printf("        class     : %u\n", rr->class);
        printf("        ttl       : %u\n", rr->ttl);
        printf("        rd_length : %u\n", rr->rd_length);
        
        resource_data_t *rd = &rr->rd_data;
        switch(rr->type){
            case A_RR_type:
                printf("        Address Resource Record:");
                for(i = 0; i < 4; ++i){
                    printf("%s%u", (i ? "." : ""), rd->a_record.addr[i]);
                }
                printf("\n");
                break;
            case NS_RR_type:
                printf("        Name Server Resource Record:\n");
                printf("            name: %s\n", rd->ns_record.name);
                break;
            case CNAME_RR_type:
                printf("        Canonical Name Resource Record:\n");
                printf("            name: %s\n", rd->cname_record.name);
                break;
            case PTR_RR_type:
                printf("        Pointer Resource Record:\n");
                printf("            name : %s\n", rd->ptr_record.name);
                break;
            case MX_RR_type:
                printf("        Mail Exchange Record:\n");
                printf("            preference: %u\n", rd->mx_record.preference);
                printf("            exchange  : %s\n", rd->mx_record.exchange);
                break;
            default:
                printf("        Unknown Resource Record ???\n");
                break;
        }
        rr = rr->next;
    }
    return;
}

void free_resource_record(resource_record_t *rr){
    if(rr == NULL){
        return;
    }
    free(rr->name);
    if(rr->type == NS_RR_type){
        free(rr->rd_data.ns_record.name);
    }else if(rr->type == CNAME_RR_type){
        free(rr->rd_data.cname_record.name);
    }else if(rr->type == PTR_RR_type){
        free(rr->rd_data.ptr_record.name);
    }else if(rr->type == MX_RR_type){
        free(rr->rd_data.mx_record.exchange);
    }
    free(rr);
}

void free_message(message_t *msg){
    if(msg == NULL){
        return;
    }
    question_t *q = msg->questions, *x;
    while(q != NULL){
        x = q;
        q = q->next;
        free(x);
    }
    resource_record_t *rr, *y;
    rr = msg->answers;
    while(rr != NULL){
        y = rr;
        rr = rr->next;
        free_resource_record(y);
    }

    rr = msg->authorities;
    while(rr != NULL){
        y = rr;
        rr = rr->next;
        free_resource_record(y);
    }

    rr = msg->additionals;
    while(rr != NULL){
        y = rr;
        rr = rr->next;
        free_resource_record(y);
    }

    free(msg);
}

resource_record_t *copy_resource_record(resource_record_t *rr){
    assert(rr != NULL);
    resource_record_t *ret = (resource_record_t *)malloc(sizeof(resource_record_t));
    memcpy(ret, rr, sizeof(resource_record_t));

    ret->name = strdup(rr->name);
    ret->next = NULL;
    if(ret->type == NS_RR_type){
        ret->rd_data.ns_record.name = strdup(rr->rd_data.ns_record.name);
    }else if(ret->type == CNAME_RR_type){
        ret->rd_data.cname_record.name = strdup(rr->rd_data.cname_record.name);
    }else if(ret->type == PTR_RR_type){
        ret->rd_data.ptr_record.name = strdup(rr->rd_data.ptr_record.name);
    }else if(ret->type == MX_RR_type){
        ret->rd_data.mx_record.exchange = strdup(rr->rd_data.mx_record.exchange);
    }
    return ret;
}

question_t *copy_question(question_t *q){
    question_t *ret = calloc(1, sizeof(question_t));
    memcpy(ret, q, sizeof(question_t));
    ret->qName = strdup(q->qName);
    ret->next = NULL;
}

bool resource_record_cmp(resource_record_t *rr1, resource_record_t *rr2){
    if(rr1->type != rr2->type || rr1->class != rr2->class){
        return false;
    }
    if(strcmp(rr1->name, rr2->name) != 0){
        return false;
    }
    if(rr1->rd_length != rr2->rd_length){
        return false;
    }

    int i;
    if(rr1->type == A_RR_type){
        for(i = 0; i < 4; ++i){
            if(rr1->rd_data.a_record.addr[i] != rr2->rd_data.a_record.addr[i]){
                return false;
            }
        }
    }else if(rr1->type == NS_RR_type){
        if(strcmp(rr1->rd_data.ns_record.name, rr2->rd_data.ns_record.name) != 0){
            return false;
        }else{
            return true;
        }
    }else if(rr1->type == CNAME_RR_type){
        if(strcmp(rr1->rd_data.cname_record.name, rr2->rd_data.cname_record.name) != 0){
            return false;
        }else{
            return true;
        }
    }else if(rr1->type == PTR_RR_type){
        if(strcmp(rr1->rd_data.ptr_record.name, rr2->rd_data.ptr_record.name) != 0){
            return false;
        }else{
            return true;
        }
    }else if(rr1->type == MX_RR_type){
        if(rr1->rd_data.mx_record.preference != rr2->rd_data.mx_record.preference){
            return false;
        }
        if(strcmp(rr1->rd_data.mx_record.exchange, rr2->rd_data.mx_record.exchange) != 0){
            return false;
        }else{
            return true;
        }
    }
    return false;
}
