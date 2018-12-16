#ifndef DNS_MSG_H
#define DNS_MSG_H

#include <stdint.h>
#include <stdbool.h>

#include "log.h"

#define DNS_STRING_OCTET_MAX 64

/* Response Type */
/* RCODE */
enum{
    OK_ResponseType = 0,
    FormatError_ResponseType = 1,
    ServerFailure_ResponseType = 2,
    NameError_ResponseType = 3,
    NotImplemented_ResponseType = 4,
    Refused_ResponseType = 5
};

/* Resource Record Types */
enum{
    A_RR_type = 1,
    NS_RR_type = 2,
    CNAME_RR_type = 5,
    PTR_RR_type = 12,
    MX_RR_type = 15,
};

/* Resource Record Class */
enum{
    IN_RR_class = 1
};

/* Response Code */
enum{
    NoError_RC
};

/* OPCODE */
enum{
    QUERY_OPCODE = 0, /* standard query */
    INVERSE_QUERY_OPCODE = 1 /* inverse query */
};

typedef struct question question_t;
typedef union resource_data resource_data_t;
typedef struct resource_record resource_record_t;
typedef struct message message_t;

/* Question Section */
struct question{
    char *qName;
    uint16_t qType;
    uint16_t qClass;
    question_t *next;   // for linked list
};

union resource_data{
    struct{
        uint8_t addr[4];
    }a_record;
    struct{
        char *name;
    }ns_record;
    struct{
        char *name;
    }cname_record;
    struct{
        char *name;
    }ptr_record;
    struct{
        uint16_t preference;
        char *exchange;
    }mx_record;
};

struct resource_record{
    char *name;
    uint16_t type;
    uint16_t class;
    uint32_t ttl;
    uint16_t rd_length;
    resource_data_t rd_data;
    resource_record_t *next;    // for linked list
};

struct message{
    uint16_t id;            /* Identifier */

    /* Flags */
    uint16_t qr;            /* Query/Response Flag */
    uint16_t opcode;        /* Operation Code */
    uint16_t aa;            /* Authoriative Answer Flag */
    uint16_t tc;            /* Truncation Flag */
    uint16_t rd;            /* Recursion Desired */
    uint16_t ra;            /* Recursion Available */
    uint16_t rcode;         /* Response Code */

    uint16_t qesCount;      /* Question Count */
    uint16_t ansCount;      /* Answer Record Count */
    uint16_t autCount;      /* Authority Record Count */
    uint16_t addCount;      /* Addtional Record Count */

    /* At least one question; questions are copied to the response 1:1 */
    question_t *questions;

    /*
     * Resource records to be send back.
     * Every resource record can be in any of the following places.
     * But every place has a different sematic.
     */
    resource_record_t *answers;
    resource_record_t *authorities;
    resource_record_t *additionals;
};

uint16_t get_rr_type_value(const char *str);
uint16_t get_rr_class_value(const char *str);
void set_rr_rd_data(resource_record_t *ptr_rr, char *str);

int encode_message(message_t *msg, uint8_t **buffer);
int decode_message(message_t *msg, uint8_t *buffer, int size);

uint8_t  get8bits(uint8_t **buffer);
uint16_t get16bits(uint8_t **buffer);
uint32_t get32bits(uint8_t **buffer);

void put8bits(uint8_t **buffer, uint8_t value);
void put16bits(uint8_t **buffer, uint16_t value);
void put32bits(uint8_t **buffer, uint32_t value);

void print_query(message_t *msg);
void print_resource_record(resource_record_t *rr);

void free_message(message_t *msg);
void free_resource_record(resource_record_t *rr);

resource_record_t *copy_resource_record(resource_record_t *rr);
question_t *copy_question(question_t *q);

bool resource_record_cmp(resource_record_t *rr1, resource_record_t *rr2);

#endif
