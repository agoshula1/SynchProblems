'''
 Based on a solution to Dining Philosophers problem given in the
 "Little Book of Semaphores" by Allen B. Downey (specifically solution #1).
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading
import random
import time

def left(i):
    return i

def right(i):
    return (i + 1) % 5

def get_forks(i):
    footman.acquire()
    forks[right(i)].acquire()
    forks[left(i)].acquire()
    print "philosopher " + str(i) + " eating"

def put_forks(i):
    forks[right(i)].release()
    forks[left(i)].release()
    footman.release()

def philosopher(i):
    #think
    time.sleep(random.randint(1,2))
    get_forks(i)
    #eat
    time.sleep(random.randint(1,2))
    put_forks(i)

forks = [threading.Semaphore(1) for i in range(5)]
footman = threading.Semaphore(4)

while(True):
    for n in range(5):
        t = threading.Thread(target=philosopher, args=(n,))
        t.start()
