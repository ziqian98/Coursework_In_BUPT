#ifndef LOG_H
#define LOG_H

#include <stdio.h>
#include <time.h>

#define LOG(format, ...)                                                        \
{                                                                               \
    time_t t = time(0);                                                         \
    struct tm ttt = *localtime(&t);                                             \
    fprintf(stdout, "[INFO] [%4d-%02d-%02d %02d:%02d:%02d] [%s:%d] " format "", \
        ttt.tm_year + 1900, ttt.tm_mon + 1, ttt.tm_mday, ttt.tm_hour,           \
        ttt.tm_min, ttt.tm_sec, __FUNCTION__ , __LINE__, ##__VA_ARGS__);        \
}

#endif
