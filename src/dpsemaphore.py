'''
 Based on a the first solution to Dining Philosophers problem given in the
 "Little Book of Semaphores" by Allen B. Downey.
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading
import random
import time

class SyncCtrl():
    def __init__(self):
        self.forks = [threading.Semaphore(1) for i in range(5)]
        self.footman = threading.Semaphore(4)

def left(i):
    return i

def right(i):
    return (i + 1) % 5

def get_forks(i,ctrl):
    ctrl.footman.acquire()
    ctrl.forks[right(i)].acquire()
    ctrl.forks[left(i)].acquire()
    #print "philosopher " + str(i) + " eating\n"

def put_forks(i,ctrl):
    ctrl.forks[right(i)].release()
    ctrl.forks[left(i)].release()
    ctrl.footman.release()

def philosopher(i,ctrl):
    #think
    #time.sleep(random.randint(0,1))
    get_forks(i,ctrl)
    #eat
    #time.sleep(random.randint(0,1))
    put_forks(i,ctrl)

def simulate(iter):
    threads = []
    for m in range(iter):
        ctrl = SyncCtrl()
        for n in range(5):
            t = threading.Thread(target=philosopher, args=(n,ctrl))
            threads.append(t)
            t.start()

        for n in range(5):
            threads[n].join()

#correctness testing
simulate(20)

#performance testing
t0 = time.clock()
simulate(20);
print "Step 1: Time elapsed (sec) = {}".format(time.clock() - t0)

t0 = time.clock()
simulate(200);
print "Step 2: Time elapsed (sec) = {}".format(time.clock() - t0)

t0 = time.clock()
simulate(2000);
print "Step 3: Time elapsed (sec) = {}".format(time.clock() - t0)
