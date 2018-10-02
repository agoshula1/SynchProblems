'''
 Based on a solution to Dining Philosophers problem given in the
 "Little Book of Semaphores" by Allen B. Downey (specifically solution #1).
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading

def left(i):
    return i

def right(i):
    return (i + 1) % 5

def get_forks(i):
    footman.acquire()
    fork[right(i)].acquire()
    fork[left(i)].acquire()

def put_forks(i):
    fork[right(i)].release()
    fork[left(i)].release()
    footman.release()

forks = [threading.Semaphore(1) for i in range(5)]
footman = threading.Semaphore(4)
