#ifndef LIST_H
#define LIST_H

#include <stdbool.h>

// a list node points to the next node in the list, and to some data
// these values can be used, but should not be *modified* outside of list.c. 
// they are read-only!
typedef struct node node_t;

struct node {
	node_t *next; // pointer to the next node in the list
				// NULL if there is no next node (the last node in the list)
	void *data; // pointer to the data held by this node
};

// a list points to its first and last nodes, and stores its size (num. nodes)
// these values can be used, but should not be *modified* outside of list.c. 
// they are read-only!
typedef struct list list_t;

struct list {
	node_t *head; // pointer to the first node in the list
	node_t *last; // pointer to the last node in the list
	int size;	// number of nodes currently in the list
};


// create a new, empty list and return its pointer
list_t *new_list();

// destroy a list and free its memory
// DOES NOT FREE POINTERS TO DATA HELD IN THE LISTS NODES, only frees the nodes
void free_list(list_t *list);

// add an element to the front of a list
// this operation is O(1)
void list_add_start(list_t *list, void *data);

// add an element to the back of a list
// this operation is O(1)
void list_add_end(list_t *list, void *data);

// remove and return the front data element from a list
// this operation is O(1)
// error if the list is empty (so first ensure list_size() > 0)
void *list_remove_start(list_t *list);

// return the front data element from a list
// this operation is O(1)
// error if the list is empty (so first ensure list_size() > 0)
void *list_get_start(list_t *list);

// remove and return the final data element in a list
// this operation is O(n), where n is the number of elements in the list
// error if the list is empty (so first ensure list_size() > 0)
void *list_remove_end(list_t *list);

// return the final data element in a list
// this operation is O(n), where n is the number of elements in the list
// error if the list is empty (so first ensure list_size() > 0)
void *list_get_end(list_t *list);

// return the number of elements contained in a list
int list_size(list_t *list);

// returns whether the list contains no elements (true) or some elements (false)
bool list_is_empty(list_t *list);

#endif
